package main

import (
	"encoding/json"
	"fmt"
	"strconv"
	"time"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

type FailInfo struct {
	Id   string `json:"id"`
	Info string `json:"info"`
}

type BatchResult struct {
	EventName string     `json:"eventName"`
	SrcMethod string     `json:"srcMethod"`
	Success   []string   `json:""success`
	Fail      []FailInfo `json:"fail"`
}

type Order struct {
	UUID         string `json:"uuid"`
	Account      string `json:"account"`
	SrcCurrency  string `json:"srcCurrency"`
	SrcCount     int64  `json:"srcCount"`
	DesCurrency  string `json:"desCurrency"`
	DesCount     int64  `json:"desCount"`
	IsBuyAll     bool   `json:"isBuyAll"`
	ExpiredTime  int64  `json:"expiredTime"`
	PendingTime  int64  `json:"PendingTime"`
	PendedTime   int64  `json:"PendedTime"`
	MatchedTime  int64  `json:"matchedTime"`
	FinishedTime int64  `json:"finishedTime"`
	RawUUID      string `json:"rawUUID"`
	Metadata     string `json:"metadata"`
	FinalCost    int64  `json:"finalCost"`
}

// initAccount init account (CNY/USD currency) when user first login
// args: user
func (c *ExchangeChaincode) initAccount() pb.Response {
	myLogger.Debug("Init account...")

	if len(c.args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}

	user := c.args[0]

	// find CNY of the user
	asset, err := c.getOwnerOneAsset(user, CNY)
	if err != nil {
		myLogger.Errorf("initAccount error1:%s", err)
		return shim.Error(fmt.Sprintf("Failed retrieving asset [%s] of the user: [%s]", CNY, err))
	}
	if asset == nil || asset.UUID == "" {
		err = c.putAsset(&Asset{
			Owner:     user,
			Currency:  CNY,
			Count:     0,
			LockCount: 0,
		})
		if err != nil {
			return shim.Error(err.Error())
		}
	}

	// fins USD of the user
	asset, err = c.getOwnerOneAsset(user, USD)
	if err != nil {
		myLogger.Errorf("initAccount error3:%s", err)
		return shim.Error(fmt.Sprintf("Failed retrieving asset [%s] of the user: [%s]", USD, err))
	}
	if asset == nil || asset.UUID == "" {
		err = c.putAsset(&Asset{
			Owner:     user,
			Currency:  USD,
			Count:     0,
			LockCount: 0,
		})
		if err != nil {
			return shim.Error(err.Error())
		}
	}

	myLogger.Debug("Init account...done")

	return shim.Success(nil)
}

// create create currency
// args:currency id, currency count, currency creator
func (c *ExchangeChaincode) create() pb.Response {
	myLogger.Debug("Create Currency...")

	if len(c.args) != 3 {
		return shim.Error("Incorrect number of arguments. Expecting 3")
	}

	id := c.args[0]
	count, _ := strconv.ParseInt(c.args[1], 10, 64)
	creator := c.args[2]
	now := time.Now().Unix()

	err := c.putCurrency(&Currency{
		ID:         id,
		Count:      count,
		LeftCount:  count,
		Creator:    creator,
		CreateTime: now,
	})
	if err != nil {
		myLogger.Errorf("create error2:%s", err)
		return shim.Error(err.Error())
	}

	if count > 0 {
		err = c.putReleaseLog(&ReleaseLog{
			Currency:    id,
			Releaser:    creator,
			Count:       count,
			ReleaseTime: now,
		})
		if err != nil {
			return shim.Error(err.Error())
		}
	}

	myLogger.Debug("Create Currency...done")

	return shim.Success(nil)
}

// release release currency
// args: currency id, release count
func (c *ExchangeChaincode) release() pb.Response {
	myLogger.Debug("Release Currency...")

	if len(c.args) != 2 {
		return shim.Error("Incorrect number of arguments. Expecting 2")
	}

	id := c.args[0]
	count, err := strconv.ParseInt(c.args[1], 10, 64)
	if err != nil || count <= 0 {
		return shim.Error("The currency release count must be > 0")
	}

	if id == CNY || id == USD {
		return shim.Error("Currency can't be CNY or USD")
	}

	curr, err := c.getCurrencyByID(id)
	if err != nil {
		myLogger.Errorf("releaseCurrency error1:%s", err)
		return shim.Error(fmt.Sprintf("Failed retrieving currency [%s]: [%s]", id, err))
	}

	// update currency data
	curr.Count = curr.Count + count
	curr.LeftCount = curr.LeftCount + count
	err = c.putCurrency(curr)
	if err != nil {
		myLogger.Errorf("releaseCurrency error2:%s", err)
		return shim.Error(fmt.Sprintf("Failed replacing row [%s]", err))
	}

	err = c.putReleaseLog(&ReleaseLog{
		Currency:    id,
		Releaser:    curr.Creator,
		Count:       count,
		ReleaseTime: time.Now().Unix(),
	})
	if err != nil {
		return shim.Error(err.Error())
	}

	myLogger.Debug("Release Currency...done")

	return shim.Success(nil)
}

// assign  assign currency
// args: json{currency id, []{reciver, count}}
func (c *ExchangeChaincode) assign() pb.Response {
	myLogger.Debug("Assign Currency...")

	if len(c.args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}

	assign := struct {
		Currency string `json:"currency"`
		Assigns  []struct {
			Owner string `json:"owner"`
			Count int64  `json:"count"`
		} `json:"assigns"`
	}{}

	err := json.Unmarshal([]byte(c.args[0]), &assign)
	if err != nil {
		myLogger.Errorf("assignCurrency error1:%s", err)
		return shim.Error(fmt.Sprintf("Failed unmarshalling assign data: [%s]", err))
	}

	if len(assign.Assigns) == 0 {
		return shim.Success(nil)
	}

	curr, err := c.getCurrencyByID(assign.Currency)
	if err != nil {
		myLogger.Errorf("assignCurrency error2:%s", err)
		return shim.Error(fmt.Sprintf("Failed retrieving currency [%s]: [%s]", assign.Currency, err))
	}

	assignCount := int64(0)
	for _, v := range assign.Assigns {
		if v.Count <= 0 {
			continue
		}

		assignCount += v.Count
		if assignCount > curr.LeftCount {
			return shim.Error(fmt.Sprintf("The left count [%d] of currency [%s] is insufficient", curr.LeftCount, assign.Currency))
		}
	}

	for _, v := range assign.Assigns {
		if v.Count <= 0 {
			continue
		}

		err = c.putAssignLog(&AssignLog{
			Currency:   assign.Currency,
			Owner:      v.Owner,
			Count:      v.Count,
			AssignTime: time.Now().Unix(),
		})
		if err != nil {
			myLogger.Errorf("assignCurrency error3:%s", err)
			return shim.Error(err.Error())
		}

		asset, err := c.getOwnerOneAsset(v.Owner, assign.Currency)
		if err != nil {
			myLogger.Errorf("assignCurrency error4:%s", err)
			return shim.Error(fmt.Sprintf("Failed retrieving asset [%s] of the user: [%s]", assign.Currency, err))
		}

		asset.Count = asset.Count + v.Count
		err = c.putAsset(asset)
		if err != nil {
			return shim.Error(err.Error())
		}

		curr.LeftCount -= v.Count
	}

	err = c.putCurrency(curr)
	if err != nil {
		return shim.Error(err.Error())
	}
	// if curr.LeftCount != currRow.Columns[2].GetInt64() {
	// 	currRow.Columns[2].Value = &shim.Column_Int64{Int64: curr.LeftCount}
	// 	_, err = c.stub.ReplaceRow(TableCurrency, currRow)
	// 	if err != nil {
	// 		myLogger.Errorf("assignCurrency error7:%s", err)
	// 		return nil, err
	// 	}
	// }

	myLogger.Debug("Assign Currency...done")
	return shim.Success(nil)
}

// lock lock or unlock user asset when commit a exchange or cancel exchange
// args: json []{user, currency id, lock count, lock order}, islock, srcMethod
func (c *ExchangeChaincode) lock() pb.Response {
	myLogger.Debug("Lock Asset Balance...")

	if len(c.args) != 3 {
		return shim.Error("Incorrect number of arguments. Expecting 3")
	}

	var lockInfos []struct {
		Owner    string `json:"owner"`
		Currency string `json:"currency"`
		OrderId  string `json:"orderId"`
		Count    int64  `json:"count"`
	}

	err := json.Unmarshal([]byte(c.args[0]), &lockInfos)
	if err != nil {
		myLogger.Errorf("lock error1:%s", err)
		return shim.Error(err.Error())
	}
	islock, _ := strconv.ParseBool(c.args[1])

	var successInfos []string
	var failInfos []FailInfo

	for _, v := range lockInfos {
		err, errType := c.lockOrUnlockBalance(v.Owner, v.Currency, v.OrderId, v.Count, islock)
		if errType == CheckErr && err != ExecedErr {
			failInfos = append(failInfos, FailInfo{Id: v.OrderId, Info: err.Error()})
			continue
		} else if errType == WorldStateErr {
			myLogger.Errorf("lock error2:%s", err)
			return shim.Error(err.Error())
		}
		successInfos = append(successInfos, v.OrderId)
	}

	batch := BatchResult{EventName: "chaincode_lock", Success: successInfos, Fail: failInfos, SrcMethod: c.args[2]}
	result, err := json.Marshal(&batch)
	if err != nil {
		myLogger.Errorf("lock error3:%s", err)
		return shim.Error(err.Error())
	}

	c.stub.SetEvent(batch.EventName, result)

	myLogger.Debug("Lock Asset Balance...done")
	return shim.Success(nil)
}

// exchange exchange asset
// args: exchange order 1, exchange order 2
func (c *ExchangeChaincode) exchange() pb.Response {
	myLogger.Debug("Exchange...")

	if len(c.args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}

	var exchangeOrders []struct {
		BuyOrder  Order `json:"buyOrder"`
		SellOrder Order `json:"sellOrder"`
	}
	err := json.Unmarshal([]byte(c.args[0]), &exchangeOrders)
	if err != nil {
		myLogger.Errorf("exchange error1:%s", err)
		return shim.Error("Failed unmarshalling order")
	}

	var successInfos []string
	var failInfos []FailInfo

	for _, v := range exchangeOrders {
		buyOrder := v.BuyOrder
		sellOrder := v.SellOrder
		matchOrder := buyOrder.UUID + "," + sellOrder.UUID

		if buyOrder.SrcCurrency != sellOrder.DesCurrency ||
			buyOrder.DesCurrency != sellOrder.SrcCurrency {
			return shim.Error("The exchange is invalid")
		}

		// check exchanged or not
		buy, err := c.getTxLog(buyOrder.UUID)
		if err != nil {
			myLogger.Errorf("exchange error2:%s", err)
			failInfos = append(failInfos, FailInfo{Id: matchOrder, Info: err.Error()})
			continue
		}
		if buy != nil && buy.UUID != "" {
			// exchanged
		}

		sell, err := c.getTxLog(sellOrder.UUID)
		if err != nil {
			myLogger.Errorf("exchange error3:%s", err)
			failInfos = append(failInfos, FailInfo{Id: matchOrder, Info: err.Error()})
			continue
		}
		if sell != nil && sell.UUID != "" {
			// exchanged
		}

		// execTx
		err, errType := c.execTx(&buyOrder, &sellOrder)
		if errType == CheckErr && err != ExecedErr {
			failInfos = append(failInfos, FailInfo{Id: matchOrder, Info: err.Error()})
			continue
		} else if errType == WorldStateErr {
			myLogger.Errorf("exchange error4:%s", err)
			return shim.Error(err.Error())
		}

		// txlog
		err = c.putTxLog(&buyOrder, &sellOrder)
		if err != nil {
			myLogger.Errorf("exchange error5:%s", err)
			return shim.Error(err.Error())
		}

		successInfos = append(successInfos, matchOrder)
	}

	batch := BatchResult{EventName: "chaincode_exchange", Success: successInfos, Fail: failInfos}
	result, err := json.Marshal(&batch)
	if err != nil {
		myLogger.Errorf("exchange error6:%s", err)
		return shim.Error(err.Error())
	}
	c.stub.SetEvent(batch.EventName, result)

	myLogger.Debug("Exchange...done")
	return shim.Success(nil)
}
