package main

import (
	"encoding/json"
	"time"

	"github.com/hyperledger/fabric/core/chaincode/shim"
)

const (
	TableCurrency           = "Currency"
	TableCurrencyReleaseLog = "CurrencyReleaseLog"
	TableCurrencyAssignLog  = "CurrencyAssignLog"
	TableAssets             = "Assets"
	TableAssetLockLog       = "AssetLockLog"
	TableTxLog              = "TxLog"
	TableTxLog2             = "TxLog2"
)

// CreateTable InitTable
func (c *ExternalityChaincode) CreateTable() error {
	// currency info
	err := c.stub.CreateTable(TableCurrency, []*shim.ColumnDefinition{
		&shim.ColumnDefinition{Name: "ID", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "Count", Type: shim.ColumnDefinition_INT64, Key: false},
		&shim.ColumnDefinition{Name: "LeftCount", Type: shim.ColumnDefinition_INT64, Key: false},
		&shim.ColumnDefinition{Name: "Creator", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "CreateTime", Type: shim.ColumnDefinition_INT64, Key: false},
	})
	if err != nil {
		myLogger.Errorf("createTable error1:%s", err)
		return err
	}

	// currency release log
	err = c.stub.CreateTable(TableCurrencyReleaseLog, []*shim.ColumnDefinition{
		&shim.ColumnDefinition{Name: "Currency", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "Count", Type: shim.ColumnDefinition_INT64, Key: false},
		&shim.ColumnDefinition{Name: "ReleaseTime", Type: shim.ColumnDefinition_INT64, Key: true},
	})
	if err != nil {
		myLogger.Errorf("createTable error2:%s", err)
		return err
	}

	// currency assign log
	err = c.stub.CreateTable(TableCurrencyAssignLog, []*shim.ColumnDefinition{
		&shim.ColumnDefinition{Name: "Currency", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "Owner", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "Count", Type: shim.ColumnDefinition_INT64, Key: false},
		&shim.ColumnDefinition{Name: "AssignTime", Type: shim.ColumnDefinition_INT64, Key: true},
	})
	if err != nil {
		myLogger.Errorf("createTable error3:%s", err)
		return err
	}

	// user asset info
	err = c.stub.CreateTable(TableAssets, []*shim.ColumnDefinition{
		&shim.ColumnDefinition{Name: "Owner", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "Currency", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "Count", Type: shim.ColumnDefinition_INT64, Key: false},
		&shim.ColumnDefinition{Name: "LockCount", Type: shim.ColumnDefinition_INT64, Key: false},
	})
	if err != nil {
		myLogger.Errorf("createTable error4:%s", err)
		return err
	}

	// user balance lock log
	err = c.stub.CreateTable(TableAssetLockLog, []*shim.ColumnDefinition{
		&shim.ColumnDefinition{Name: "Owner", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "Currency", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "Order", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "IsLock", Type: shim.ColumnDefinition_BOOL, Key: true},
		&shim.ColumnDefinition{Name: "LockCount", Type: shim.ColumnDefinition_INT64, Key: false},
		&shim.ColumnDefinition{Name: "LockTime", Type: shim.ColumnDefinition_INT64, Key: false},
	})
	if err != nil {
		myLogger.Errorf("createTable error5:%s", err)
		return err
	}

	// tx log
	err = c.stub.CreateTable(TableTxLog, []*shim.ColumnDefinition{
		&shim.ColumnDefinition{Name: "Owner", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "SrcCurrency", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "DesCurrency", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "RawOrder", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "Detail", Type: shim.ColumnDefinition_BYTES, Key: true},
	})
	if err != nil {
		myLogger.Errorf("createTable error6:%s", err)
		return err
	}

	// tx log
	err = c.stub.CreateTable(TableTxLog2, []*shim.ColumnDefinition{
		&shim.ColumnDefinition{Name: "UUID", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "Detail", Type: shim.ColumnDefinition_BYTES, Key: false},
	})
	if err != nil {
		myLogger.Errorf("createTable error7:%s", err)
		return err
	}
	return nil
}
func (c *ExchangeChaincode) initCurrency() error {

	cur := Currency{
		ID:         CNY,
		Count:      0,
		LeftCount:  0,
		Creator:    "system",
		CreateTime: time.Now().Unix(),
	}

	cny, err := json.Marshal(&cur)
	if err != nil {
		return err
	}
	err = c.stub.PutState(CNY, cny)
	if err != nil {
		return err
	}

	cur.ID = USD
	usd, err := json.Marshal(&cur)
	if err != nil {
		return err
	}
	err = c.stub.PutState(USD, usd)
	if err != nil {
		return err
	}

	return nil
}
