package main

import (
	"crypto/rand"
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"strconv"
	"time"

	"github.com/hyperledger/fabric/core/chaincode/shim"
)

// Asset Asset
type Asset struct {
	UUID      string `json:"uuid"`
	Owner     string `json:"owner"`
	Currency  string `json:"currency"`
	Count     int64  `json:"count"`
	LockCount int64  `json:"lockCount"`
}

// Currency Currency
type Currency struct {
	UUID       string `json:"uuid"`
	ID         string `json:"id"`
	Count      int64  `json:"count"`
	LeftCount  int64  `json:"leftCount"`
	Creator    string `json:"creator"`
	CreateTime int64  `json:"createTime"`
}

type ErrType string

const (
	CheckErr      = ErrType("CheckErr")
	WorldStateErr = ErrType("WdErr")
)

var (
	ExecedErr = errors.New("execed")
	NoDataErr = errors.New("No row data")
)

// type Key struct {
// 	UUID string `json:"uuid"`
// }

// type stateOpt interface {
// 	Put(stub shim.ChaincodeStubInterface, value interface{}) error
// 	Get(stub shim.ChaincodeStubInterface) (interface{}, error)
// }

// func (k *Key) Put(stub shim.ChaincodeStubInterface, value interface{}) error {
// 	b, err := json.Marshal(value)
// 	if err != nil {
// 		return err
// 	}
// 	return stub.PutState(k.UUID, b)
// }

// func (k *Key) Get(stub shim.ChaincodeStubInterface) (interface{}, error) {

// }
func (c *ExchangeChaincode) putAsset(asset *Asset) error {
	if asset.UUID == "" {
		asset.UUID = GenerateUUID()
	}
	r, err := json.Marshal(asset)
	if err != nil {
		return err
	}

	err = c.stub.PutState(asset.UUID, r)
	if err != nil {
		return err
	}

	indexName := "owner~currency~uuid"
	indexKey, err := c.stub.CreateCompositeKey(indexName, []string{asset.Owner, asset.Currency, asset.UUID})
	if err != nil {
		return err
	}

	err = c.stub.PutState(indexKey, []byte{0x00})
	if err != nil {
		return err
	}

	indexName2 := "owner~uuid"
	indexKey2, err := c.stub.CreateCompositeKey(indexName2, []string{asset.Owner, asset.UUID})
	if err != nil {
		return err
	}
	err = c.stub.PutState(indexKey2, []byte{0x00})
	if err != nil {
		return err
	}
	return nil
}

func (c *ExchangeChaincode) getAsset(key string) (*Asset, error) {
	assetByte, err := c.stub.GetState(key)
	if err != nil {
		return nil, err
	}

	var asset *Asset
	err = json.Unmarshal(assetByte, asset)
	if err != nil {
		return nil, err
	}
	return asset, nil
}

// getOwnerOneAsset
func (c *ExchangeChaincode) getOwnerOneAsset(owner, currency string) (*Asset, error) {
	var asset *Asset

	resultsIterator, err := c.stub.GetStateByPartialCompositeKey("owner~currency~uuid", []string{owner, currency})
	if err != nil {
		return nil, err
	}
	defer resultsIterator.Close()

	for resultsIterator.HasNext() {
		responseRange, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}

		_, compositeKeyParts, err := c.stub.SplitCompositeKey(responseRange.Key)
		if err != nil {
			return nil, err
		}

		uuid := compositeKeyParts[2]
		return c.getAsset(uuid)
	}

	return nil, nil
}

// getOwnerAllAsset
func (c *ExchangeChaincode) getOwnerAllAsset(owner string) ([]*Asset, error) {

	var assets []*Asset

	resultsIterator, err := c.stub.GetStateByPartialCompositeKey("owner~uuid", []string{owner})
	if err != nil {
		return nil, err
	}
	defer resultsIterator.Close()

	for resultsIterator.HasNext() {
		responseRange, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}

		_, compositeKeyParts, err := c.stub.SplitCompositeKey(responseRange.Key)
		if err != nil {
			return nil, err
		}

		uuid := compositeKeyParts[1]
		asset, err := c.getAsset(uuid)
		if err != nil {
			return nil, err
		}
		assets = append(assets, asset)
	}

	return assets, nil
}

// putCurrency putCurrency
func (c *ExchangeChaincode) putCurrency(currency *Currency) error {
	if currency.UUID == "" {
		currency.UUID = GenerateUUID()
	}
	r, err := json.Marshal(currency)
	if err != il {
		return err
	}

	err = c.stub.PutState(currency.UUID, r)
	if err != nil {
		return err
	}

	indexName := "id~uuid"
	indexKey, err := c.stub.CreateCompositeKey(indexName, []string{currency.ID, currency.UUID})
	if err != nil {
		return err
	}

	err = c.stub.PutState(indexKey, []byte{0x00})
	if err != nil {
		return err
	}

	return nil
}

func (c *ExchangeChaincode) getCurrency(key string) (*Currency, error) {
	currByte, err := c.stub.GetState(key)
	if err != nil {
		return nil, err
	}

	var curr *Currency
	err = json.Unmarshal(currByte, curr)
	if err != nil {
		return nil, err
	}
	return curr, nil
}

// getCurrencyByID
func (c *ExchangeChaincode) getCurrencyByID(id string) (*Currency, error) {
	var currency *Currency

	resultsIterator, err := c.stub.GetStateByPartialCompositeKey("id~uuid", []string{id})
	if err != nil {
		return nil, err
	}
	defer resultsIterator.Close()

	for resultsIterator.HasNext() {
		responseRange, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}

		_, compositeKeyParts, err := c.stub.SplitCompositeKey(responseRange.Key)
		if err != nil {
			return nil, err
		}

		uuid := compositeKeyParts[1]
		return c.getCurrency(uuid)
	}

	return nil, nil
}

// getAllCurrency
func (c *ExchangeChaincode) getAllCurrency() ([]*Currency, error) {
	rowChannel, err := c.stub.GetRows(TableCurrency, nil)
	if err != nil {
		return nil, nil, fmt.Errorf("getRows operation failed. %s", err)
	}
	var rows []shim.Row
	var infos []*Currency

	resultsIterator, err := c.stub.GetStateByRange("", "")
	if err != nil {
		return nil, err
	}
	defer resultsIterator.Close()

	for resultsIterator.HasNext() {
		responseRange, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}

		err = json.Unmarshal(responseRange.Value, curr)
		if err != nil {
			return nil, err
		}
		currs = append(currs, curr)
	}

	return currs, nil
}

type ReleaseLog struct {
	UUID        string `json:"uuid"`
	Currency    string `json:"currency"`
	Count       int64  `json:"cont"`
	ReleaseTime int64  `json:"releaseTime"`
}

// saveReleaseLog
func (c *ExchangeChaincode) putReleaseLog(log *ReleaseLog) error {
	if log.UUID == "" {
		log.UUID = GenerateUUID()
	}
	r, err := json.Marshal(log)
	if err != nil {
		return err
	}

	err = c.stub.PutState(log.UUID, r)
	if err != nil {
		return err
	}
	return nil
}

type AssignLog struct {
	UUID       string `json:"uuid"`
	Currency   string `json:"currency"`
	Owner      string `json:"owner"`
	Count      int64  `json:"count"`
	AssignTime int64  `json:"assignTime"`
}

// saveAssignLog
func (c *ExchangeChaincode) putAssignLog(log *AssignLog) error {
	if log.UUID == "" {
		log.UUID = GenerateUUID()
	}
	r, err := json.Marshal(log)
	if err != nil {
		return err
	}

	err = c.stub.PutState(log.UUID, r)
	if err != nil {
		return err
	}
	return nil
}

// lockOrUnlockBalance lockOrUnlockBalance
func (c *ExchangeChaincode) lockOrUnlockBalance(owner string, currency, order string, count int64, islock bool) (error, ErrType) {
	asset, err := c.getOwnerOneAsset(owner, currency)
	if err != nil {
		return fmt.Errorf("Failed retrieving asset [%s] of the user: [%s]", currency, err), CheckErr
	}
	if asset == nil || asset.UUID == "" {
		return fmt.Errorf("The user have not currency [%s]", currency), CheckErr
	}
	if islock && asset.Count < count {
		return fmt.Errorf("Currency [%s] of the user is insufficient", currency), CheckErr
	} else if !islock && asset.LockCount < count {
		return fmt.Errorf("Locked currency [%s] of the user is insufficient", currency), CheckErr
	}

	// check the order is locked/unlocked or not
	lockLog, err := c.getLockLogByParm(owner, currency, order, islock)
	if err != nil {
		return err, CheckErr
	}

	if lockLog != nil && lockLog.UUID != "" {
		return ExecedErr, CheckErr
	}

	if islock {
		asset.Count = asset.Count - count
		asset.LockCount = asset.LockCount + count
	} else {
		asset.Count = asset.Count + count
		asset.LockCount = asset.LockCount - count
	}

	err = c.putAsset(asset)
	if err != nil {
		return err, WorldStateErr
	}

	err = c.putLockLog(&LockLog{
		Owner:     owner,
		Currency:  currency,
		Order:     order,
		IsLock:    islock,
		LockCount: count,
		LockTime:  time.Now().Unix(),
	})
	if err != nil {
		return err, WorldStateErr
	}

	return nil, ErrType("")
}

type LockLog struct {
	UUID      string `json:"uuid"`
	Owner     string `json:"owner"`
	Currency  string `json:"currency"`
	Order     string `json:"order"`
	IsLock    bool   `json:"isLock"`
	LockCount int64  `json:"lockCount"`
	LockTime  int64  `json:"lockTime"`
}

func (c *ExchangeChaincode) putLockLog(log *LockLog) error {
	if log.UUID == "" {
		log.UUID = GenerateUUID()
	}
	r, err := json.Marshal(log)
	if err != nil {
		return err
	}

	err = c.stub.PutState(log.UUID, r)
	if err != nil {
		return err
	}

	indexName := "owner~curr~order~islock~uuid"
	indexKey, err := c.stub.CreateCompositeKey(indexName, []string{log.Owner, log.Currency, log.Order, strconv.FormatBool(log.IsLock), log.UUID})
	if err != nil {
		return err
	}

	err = c.stub.PutState(indexKey, []byte{0x00})
	if err != nil {
		return err
	}
	return nil
}

// getLockLog getLockLog
func (c *ExchangeChaincode) getLockLog(key string) (*LockLog, error) {
	logByte, err := c.stub.GetState(key)
	if err != nil {
		return nil, err
	}

	var log *LockLog
	err = json.Unmarshal(logByte, curr)
	if err != nil {
		return nil, err
	}
	return log, nil
}

// getLockLog getLockLog
func (c *ExchangeChaincode) getLockLogByParm(owner, currency, order string, islock bool) (*LockLog, error) {
	var log *LockLog

	resultsIterator, err := c.stub.GetStateByPartialCompositeKey("owner~curr~order~islock~uuid", []string{owner, currency, order, strconv.FormatBool(islock)})
	if err != nil {
		return nil, err
	}
	defer resultsIterator.Close()

	for resultsIterator.HasNext() {
		responseRange, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}

		_, compositeKeyParts, err := c.stub.SplitCompositeKey(responseRange.Key)
		if err != nil {
			return nil, err
		}

		uuid := compositeKeyParts[4]
		return c.getLockLog(uuid)
	}
	return nil, nil
}

// execTx execTx
func (c *ExchangeChaincode) execTx(buyOrder, sellOrder *Order) (error, ErrType) {
	// UUID=rawuuID
	if buyOrder.IsBuyAll && buyOrder.UUID == buyOrder.RawUUID {
		unlock, err := c.computeBalance(buyOrder.Account, buyOrder.SrcCurrency, buyOrder.DesCurrency, buyOrder.RawUUID, buyOrder.FinalCost)
		if err != nil {
			myLogger.Errorf("execTx error1:%s", err)
			return errors.New("Failed compute balance"), CheckErr
		}
		myLogger.Debugf("Order %s balance %d", buyOrder.UUID, unlock)
		if unlock > 0 {
			err, errType := c.lockOrUnlockBalance(buyOrder.Account, buyOrder.SrcCurrency, buyOrder.RawUUID, unlock, false)
			if err != nil {
				myLogger.Errorf("execTx error2:%s", err)
				return errors.New("Failed unlock balance"), errType
			}
		}
	}

	// buy order srcCurrency -
	buySrcRow, buySrcAsset, err := c.getOwnerOneAsset(buyOrder.Account, buyOrder.SrcCurrency)
	if err != nil {
		myLogger.Errorf("execTx error3:%s", err)
		return fmt.Errorf("Failed retrieving asset [%s] of the user: [%s]", buyOrder.SrcCurrency, err), CheckErr
	}
	if len(buySrcRow.Columns) == 0 {
		return fmt.Errorf("The user have not currency [%s]", buyOrder.SrcCurrency), CheckErr
	}
	buySrcRow.Columns[3].Value = &shim.Column_Int64{Int64: buySrcAsset.LockCount - buyOrder.FinalCost}
	_, err = c.stub.ReplaceRow(TableAssets, buySrcRow)
	if err != nil {
		myLogger.Errorf("execTx error4:%s", err)
		return errors.New("Failed updating row"), WorldStateErr
	}

	// buy order srcCurrency +
	buyDesRow, buyDesAsset, err := c.getOwnerOneAsset(buyOrder.Account, buyOrder.DesCurrency)
	if err != nil {
		myLogger.Errorf("execTx error5:%s", err)
		return fmt.Errorf("Failed retrieving asset [%s] of the user: [%s]", buyOrder.DesCurrency, err), CheckErr
	}
	if len(buyDesRow.Columns) == 0 {
		_, err := c.stub.InsertRow(TableAssets,
			shim.Row{
				Columns: []*shim.Column{
					&shim.Column{Value: &shim.Column_String_{String_: buyOrder.Account}},
					&shim.Column{Value: &shim.Column_String_{String_: buyOrder.DesCurrency}},
					&shim.Column{Value: &shim.Column_Int64{Int64: buyOrder.DesCount}},
					&shim.Column{Value: &shim.Column_Int64{Int64: int64(0)}},
				},
			})
		if err != nil {
			myLogger.Errorf("execTx error6:%s", err)
			return errors.New("Failed inserting row"), WorldStateErr
		}
	} else {
		buyDesRow.Columns[2].Value = &shim.Column_Int64{Int64: buyDesAsset.Count + buyOrder.DesCount}
		_, err = c.stub.ReplaceRow(TableAssets, buyDesRow)
		if err != nil {
			myLogger.Errorf("execTx error7:%s", err)
			return errors.New("Failed updating row"), WorldStateErr
		}
	}

	// UUID=rawuuid
	if sellOrder.IsBuyAll && sellOrder.UUID == sellOrder.RawUUID {
		unlock, err := c.computeBalance(sellOrder.Account, sellOrder.SrcCurrency, sellOrder.DesCurrency, sellOrder.RawUUID, sellOrder.FinalCost)
		if err != nil {
			myLogger.Errorf("execTx error8:%s", err)
			return errors.New("Failed compute balance"), CheckErr
		}
		myLogger.Debugf("Order %s balance %d", sellOrder.UUID, unlock)
		if unlock > 0 {
			err, errType := c.lockOrUnlockBalance(sellOrder.Account, sellOrder.SrcCurrency, sellOrder.RawUUID, unlock, false)
			if err != nil {
				myLogger.Errorf("execTx error9:%s", err)
				return errors.New("Failed unlock balance"), errType
			}
		}
	}

	// sell order srcCurrency -
	sellSrcRow, sellSrcAsset, err := c.getOwnerOneAsset(sellOrder.Account, sellOrder.SrcCurrency)
	if err != nil {
		myLogger.Errorf("execTx error10:%s", err)
		return fmt.Errorf("Failed retrieving asset [%s] of the user: [%s]", sellOrder.SrcCurrency, err), CheckErr
	}
	if len(sellSrcRow.Columns) == 0 {
		return fmt.Errorf("The user have not currency [%s]", sellOrder.SrcCurrency), CheckErr
	}
	sellSrcRow.Columns[3].Value = &shim.Column_Int64{Int64: sellSrcAsset.LockCount - sellOrder.FinalCost}
	_, err = c.stub.ReplaceRow(TableAssets, sellSrcRow)
	if err != nil {
		myLogger.Errorf("execTx error11:%s", err)
		return errors.New("Failed updating row"), WorldStateErr
	}

	// sell order desCurrency +
	sellDesRow, sellDesAsset, err := c.getOwnerOneAsset(sellOrder.Account, sellOrder.DesCurrency)
	if err != nil {
		myLogger.Errorf("execTx error12:%s", err)
		return fmt.Errorf("Failed retrieving asset [%s] of the user: [%s]", sellOrder.DesCurrency, err), CheckErr
	}
	if len(sellDesRow.Columns) == 0 {
		_, err = c.stub.InsertRow(TableAssets,
			shim.Row{
				Columns: []*shim.Column{
					&shim.Column{Value: &shim.Column_String_{String_: sellOrder.Account}},
					&shim.Column{Value: &shim.Column_String_{String_: sellOrder.DesCurrency}},
					&shim.Column{Value: &shim.Column_Int64{Int64: sellOrder.DesCount}},
					&shim.Column{Value: &shim.Column_Int64{Int64: 0}},
				},
			})
		if err != nil {
			myLogger.Errorf("execTx error13:%s", err)
			return errors.New("Failed inserting row"), WorldStateErr
		}
	} else {
		sellDesRow.Columns[2].Value = &shim.Column_Int64{Int64: sellDesAsset.Count + sellOrder.DesCount}
		_, err = c.stub.ReplaceRow(TableAssets, sellDesRow)
		if err != nil {
			myLogger.Errorf("execTx error14:%s", err)
			return errors.New("Failed updating row"), WorldStateErr
		}
	}
	return nil, ErrType("")
}

// computeBalance
func (c *ExchangeChaincode) computeBalance(owner string, srcCurrency, desCurrency, rawUUID string, currentCost int64) (int64, error) {
	txs, err := c.getTXs(owner, srcCurrency, desCurrency, rawUUID)
	if err != nil {
		return 0, err
	}
	lockLog, err := c.getLockLogByParm(owner, srcCurrency, rawUUID, true)
	if err != nil {
		return 0, err
	}
	if lockLog == nil || lockLog.UUID == "" {
		return 0, errors.New("can't find lock log")
	}

	lock := lockLog.LockCount
	sumCost := int64(0)
	for _, tx := range txs {
		sumCost += tx.FinalCost
	}

	return lock - sumCost - currentCost, nil
}

// putTxLog
func (c *ExchangeChaincode) putTxLog(buyOrder, sellOrder *Order) error {
	buyJson, err := json.Marshal(buyOrder)
	if err != nil {
		return err
	}
	sellJson, err := json.Marshal(sellOrder)
	if err != nil {
		return err
	}

	err = c.stub.PutState(buyOrder.UUID, buyJson)
	if err != nil {
		return err
	}

	err = c.stub.PutState(sellOrder.UUID, sellJson)
	if err != nil {
		return err
	}

	indexName := "owner~src~des~raw~uuid"
	buyKey, err := c.stub.CreateCompositeKey(indexName, []string{buyOrder.Account, buyOrder.SrcCurrency, buyOrder.DesCurrency, buyOrder.RawUUID, buyOrder.UUID})
	if err != nil {
		return err
	}
	err = c.stub.PutState(buyKey, []byte{0x00})
	if err != nil {
		return err
	}

	sellKey, err := c.stub.CreateCompositeKey(indexName, []string(sellOrder.Account, sellOrder.SrcCurrency, sellOrder.DesCurrency, sellOrder.RawUUID, sellOrder.UUID))
	if err != nil {
		return err
	}
	err = c.stub.PutState(sellKey, []byte{0x00})
	if err != nil {
		return err
	}
	return nil
}

// getTxLog
func (c *ExchangeChaincode) getTxLog(key string) (*Order, error) {
	orderByte, err := c.stub.GetState(key)
	if err != nil {
		return nil, err
	}

	var order *Order
	err = json.Unmarshal(orderByte, order)
	if err != nil {
		return nil, err
	}
	return order, nil
}

// getTXs
func (c *ExchangeChaincode) getTXs(owner, srcCurrency, desCurrency, rawOrder string) ([]*Order, error) {
	var orders []*Order

	resultsIterator, err := c.stub.GetStateByPartialCompositeKey("owner~src~des~raw~uuid", []string(owner, srcCurrency, desCurrency, rawOrder))
	if err != nil {
		return nil, err
	}
	defer resultsIterator.Close()

	for resultsIterator.HasNext() {
		responseRange, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}

		_, compositeKeyParts, err := c.stub.SplitCompositeKey(responseRange.Key)
		if err != nil {
			return nil, err
		}

		uuid := compositeKeyParts[4]
		order, err := c.getTxLog(uuid)
		if err != nil {
			return nil, err
		}
		orders = append(orders, order)
	}

	return orders, nil
}

// getMyCurrency
func (c *ExchangeChaincode) getMyCurrency(owner string) ([]*Currency, error) {
	_, infos, err := c.getAllCurrency()
	if err != nil {
		return nil, err
	}
	if len(infos) == 0 {
		return nil, NoDataErr
	}

	var currencys []*Currency
	for _, v := range infos {
		if owner == v.Creator {
			currencys = append(currencys, v)
		}
	}

	return currencys, nil
}

func dealParam(function string, args []string) (string, []string) {
	function_b, err := base64.StdEncoding.DecodeString(function)
	if err != nil {
		return function, args
	}
	for k, v := range args {
		arg_b, err := base64.StdEncoding.DecodeString(v)
		if err != nil {
			return function, args
		}
		args[k] = string(arg_b)
	}

	return string(function_b), args
}

// GenerateBytesUUID returns a UUID based on RFC 4122 returning the generated bytes
func GenerateBytesUUID() []byte {
	uuid := make([]byte, 16)
	_, err := io.ReadFull(rand.Reader, uuid)
	if err != nil {
		panic(fmt.Sprintf("Error generating UUID: %s", err))
	}

	// variant bits; see section 4.1.1
	uuid[8] = uuid[8]&^0xc0 | 0x80

	// version 4 (pseudo-random); see section 4.1.3
	uuid[6] = uuid[6]&^0xf0 | 0x40

	return uuid
}

// GenerateUUID returns a UUID based on RFC 4122
func GenerateUUID() string {
	uuid := GenerateBytesUUID()
	return idBytesToStr(uuid)
}

func idBytesToStr(id []byte) string {
	return fmt.Sprintf("%x-%x-%x-%x-%x", id[0:4], id[4:6], id[6:8], id[8:10], id[10:])
}
