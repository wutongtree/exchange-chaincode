package main

import (
	"encoding/json"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

type Assign struct {
	Currency   string `json:"currency`
	Owner      string `json:"owner"`
	Count      int64  `json:"count"`
	AssignTime int64  `json:"assignTime"`
}
type AssignLogs struct {
	ToMe []*Assign `json:"toMe"`
	MeTo []*Assign `json:"meTo"`
}

// queryCurrency
func (c *ExchangeChaincode) queryCurrencyByID() pb.Response {
	myLogger.Debug("queryCurrency...")

	if len(c.args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}

	id := c.args[0]

	currency, err := c.getCurrencyByID(id)
	if err != nil {
		myLogger.Errorf("queryCurrencyByID error1:%s", err)
		return shim.Error(err.Error())
	}
	if currency == nil {
		return shim.Error(NoDataErr.Error())
	}
	payload, err := json.Marshal(&currency)
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(payload)
}

// queryAllCurrency
func (c *ExchangeChaincode) queryAllCurrency() pb.Response {
	myLogger.Debug("queryCurrency...")

	if len(c.args) != 0 {
		return shim.Error("Incorrect number of arguments. Expecting 0")
	}

	infos, err := c.getAllCurrency()
	if err != nil {
		return shim.Error(err.Error())
	}
	if len(infos) == 0 {
		return shim.Error(NoDataErr.Error())
	}

	payload, err := json.Marshal(&infos)
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(payload)
}

// queryTxLogs
func (c *ExchangeChaincode) queryTxLogs() pb.Response {
	myLogger.Debug("queryTxLogs...")

	if len(c.args) != 0 {
		return shim.Error("Incorrect number of arguments. Expecting 0")
	}

	infos, err := c.getAllTxLog()
	if err != nil {
		return shim.Error(err.Error())
	}
	if len(infos) == 0 {
		return shim.Error(NoDataErr.Error())
	}

	payload, err := json.Marshal(&infos)
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(payload)
}

// queryAssetByOwner
func (c *ExchangeChaincode) queryAssetByOwner() pb.Response {
	myLogger.Debug("queryAssetByOwner...")

	if len(c.args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}

	owner := c.args[0]
	assets, err := c.getOwnerAllAsset(owner)
	if err != nil {
		myLogger.Errorf("queryAssetByOwner error1:%s", err)
		return shim.Error(err.Error())
	}
	if len(assets) == 0 {
		return shim.Error(NoDataErr.Error())
	}
	payload, err := json.Marshal(&assets)
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(payload)
}

// queryMyCurrency
func (c *ExchangeChaincode) queryMyCurrency() pb.Response {
	myLogger.Debug("queryCurrency...")

	if len(c.args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}

	owner := c.args[0]
	currencys, err := c.getMyCurrency(owner)
	if err != nil {
		return shim.Error(err.Error())
	}

	payload, err := json.Marshal(&currencys)
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(payload)
}

// queryReleaseLog
func (c *ExchangeChaincode) queryMyReleaseLog() pb.Response {
	myLogger.Debug("queryMyReleaseLog...")

	if len(c.args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}
	owner := c.args[0]
	logs, err := c.getMyReleaseLog(owner)
	if err != nil {
		return shim.Error(err.Error())
	}

	payload, err := json.Marshal(logs)
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(payload)
}

// queryMyAssignLog
func (c *ExchangeChaincode) queryMyAssignLog() pb.Response {
	myLogger.Debug("queryAssignLog...")

	if len(c.args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}
	owner := c.args[0]
	logs, err := c.getMyAssignLog(owner)
	if err != nil {
		return shim.Error(err.Error())
	}
	// currencys, err := c.getMyCurrency(owner)
	// if err != nil {
	// 	return nil, err
	// }

	// logs := &AssignLog{}

	// rowChannel, err := c.stub.GetRows(TableCurrencyAssignLog, nil)
	// if err != nil {
	// 	return nil, fmt.Errorf("getRows operation failed. %s", err)
	// }

	// for {
	// 	select {
	// 	case row, ok := <-rowChannel:
	// 		if !ok {
	// 			rowChannel = nil
	// 		} else {
	// 			assign := &Assign{
	// 				Currency:   row.Columns[0].GetString_(),
	// 				Owner:      row.Columns[1].GetString_(),
	// 				Count:      row.Columns[2].GetInt64(),
	// 				AssignTime: row.Columns[3].GetInt64(),
	// 			}

	// 			if assign.Owner == owner {
	// 				logs.ToMe = append(logs.ToMe, assign)
	// 			}
	// 			for _, v := range currencys {
	// 				if v.ID == assign.Currency {
	// 					logs.MeTo = append(logs.MeTo, assign)
	// 				}
	// 			}
	// 		}
	// 	}
	// 	if rowChannel == nil {
	// 		break
	// 	}
	// }

	payload, err := json.Marshal(logs)
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(payload)
}
