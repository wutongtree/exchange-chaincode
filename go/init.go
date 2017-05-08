package main

import (
	"encoding/json"
	"time"
)

const (
	CNY = "CNY"
	USD = "USD"
)

func (c *ExchangeChaincode) initCurrency() error {

	curCNY := Currency{
		Name:       CNY,
		Count:      0,
		LeftCount:  0,
		Creator:    "system",
		CreateTime: time.Now().Unix(),
	}

	cny, err := json.Marshal(&curCNY)
	if err != nil {
		return err
	}
	err = c.stub.PutState(curCNY.Name, cny)
	if err != nil {
		return err
	}

	curUSD := Currency{
		Name:       USD,
		Count:      0,
		LeftCount:  0,
		Creator:    "system",
		CreateTime: time.Now().Unix(),
	}
	usd, err := json.Marshal(&curUSD)
	if err != nil {
		return err
	}
	err = c.stub.PutState(curUSD.Name, usd)
	if err != nil {
		return err
	}

	return nil
}
