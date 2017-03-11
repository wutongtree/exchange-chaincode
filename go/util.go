package main

import (
	"errors"
	"time"

	"github.com/hyperledger/fabric/core/chaincode/shim"
)

// Asset Asset
type Asset struct {
	Owner     string `json:"owner"`
	Currency  string `json:"currency"`
	Count     int64  `json:"count"`
	LockCount int64  `json:"lockCount"`
}

// Currency Currency
type Currency struct {
	ID         string `json:"id"`
	Count      int64  `json:"count"`
	LeftCount  int64  `json:"leftCount"`
	Creator    string `json:"creator"`
	CreateTime int64  `json:"createTime"`
}

func (c *ExternalityChaincode) getOwnerOneAsset(owner string, currency string) (shim.Row, *Asset, error) {
	var asset *Asset

	row, err := c.stub.GetRow(TableAssets, []shim.Column{
		shim.Column{Value: &shim.Column_String_{String_: owner}},
		shim.Column{Value: &shim.Column_String_{String_: currency}},
	})

	if len(row.Columns) > 0 {
		asset = &Asset{
			Owner:     row.Columns[0].GetString_(),
			Currency:  row.Columns[1].GetString_(),
			Count:     row.Columns[2].GetInt64(),
			LockCount: row.Columns[3].GetInt64(),
		}
	}

	return row, asset, err
}

func (c *ExternalityChaincode) saveReleaseLog(id string, count, now int64) error {
	ok, err := c.stub.InsertRow(TableCurrencyReleaseLog,
		shim.Row{
			Columns: []*shim.Column{
				&shim.Column{Value: &shim.Column_String_{String_: id}},
				&shim.Column{Value: &shim.Column_Int64{Int64: count}},
				&shim.Column{Value: &shim.Column_Int64{Int64: now}},
			},
		})
	if !ok {
		return errors.New("Currency was already releassed")
	}

	return err
}

func (c *ExternalityChaincode) getCurrencyByID(id string) (shim.Row, *Currency, error) {
	var currency *Currency

	row, err := c.stub.GetRow(TableCurrency, []shim.Column{
		shim.Column{Value: &shim.Column_String_{String_: id}},
	})

	if len(row.Columns) > 0 {
		currency = &Currency{
			ID:         row.Columns[0].GetString_(),
			Count:      row.Columns[1].GetInt64(),
			LeftCount:  row.Columns[2].GetInt64(),
			Creator:    row.Columns[3].GetString_(),
			CreateTime: row.Columns[4].GetInt64(),
		}
	}
	return row, currency, err
}

func (c *ExternalityChaincode) saveAssignLog(id, reciver string, count int64) error {
	_, err := c.stub.InsertRow(TableCurrencyAssignLog,
		shim.Row{
			Columns: []*shim.Column{
				&shim.Column{Value: &shim.Column_String_{String_: id}},
				&shim.Column{Value: &shim.Column_String_{String_: reciver}},
				&shim.Column{Value: &shim.Column_Int64{Int64: count}},
				&shim.Column{Value: &shim.Column_Int64{Int64: time.Now().Unix()}},
			},
		})

	return err
}
