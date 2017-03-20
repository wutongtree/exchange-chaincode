package krcc;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.java.shim.ChaincodeStub;
import org.hyperledger.protos.TableProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zerppen on 3/19/17.
 */
public class query {
    private static Log log = LogFactory.getLog(init.class);


    public String queryCurrencyByID(ChaincodeStub stub,String[] args){

        log.info("****** in queryCurrencyByID ******");
        if(args.length!=1){
            return "Incorrect number of arguments. Expecting 1";
        }
        String id = args[0];
        TableProto.Row cuRow = getCurrencyByID(stub,id);
        if(cuRow==null){
            log.error("queryCurrencyByID error1");
            return "queryCurrencyByID error1";
        }
        if(cuRow.getColumnsCount()==0){
            return "no data can be queryed";
        }
        JSONObject cuJson = new JSONObject();
        cuJson.put("id",cuRow.getColumns(0).getString());
        cuJson.put("count",cuRow.getColumns(1).getInt64());
        cuJson.put("leftCount",cuRow.getColumns(2).getInt64());
        cuJson.put("creator",cuRow.getColumns(3).getString());
        cuJson.put("createTime",cuRow.getColumns(4).getInt64());

        log.info("****** done queryCurrencyByID ******");

        return cuJson.toString();
    }

    public String queryAllCurrency(ChaincodeStub stub,String[] args){

        log.info("****** in queryAllCurrency ******");
        if(args.length!=0){
            log.error("incorrect number of arguments");
            return "incorrect number of arguments";
        }
        ArrayList<TableProto.Row> rows = null;
        try {

            List<TableProto.Column> key = new ArrayList<TableProto.Column>();
            rows = stub.getRows(util.getTableCurrency(),key);
        }catch(Exception e){
            return "getRows operation failed";
        }
        if(rows.size()==0){
            log.info("rows.size = 0");
            return "no data can be queryed";
        }
        JSONArray jArray = new JSONArray();
        for(int i= 0;i<rows.size();i++){
            JSONObject jObject = new JSONObject();
            jObject.put("id",rows.get(i).getColumns(0).getString());
            jObject.put("count",rows.get(i).getColumns(1).getInt64());
            jObject.put("leftCount",rows.get(i).getColumns(2).getInt64());
            jObject.put("creator",rows.get(i).getColumns(3).getString());
            jObject.put("createTime",rows.get(i).getColumns(4).getInt64());
            jArray.add(jObject);
        }
        if(jArray.size()==0){
            log.info("****** in queryAllCurrency ******");

            return "no data can be queryed";
        }
        log.info("****** done queryAllCurrency ******");


        return jArray.toString();
    }
    public String queryTxLogs(ChaincodeStub stub,String[] args){

        log.info("****** in queryTxLogs ******");
        if(args.length!=0){
            return "Incorrect number of arguments";
        }
        ArrayList<TableProto.Row> rows = null;
        try {

            List<TableProto.Column> key = new ArrayList<TableProto.Column>();


            rows = stub.getRows(util.getTableTxLog2(),key);
        }catch(Exception e){
            return "getRows operation failed";
        }
        JSONArray jArray = new JSONArray();
        for(int i=0;i<rows.size();i++){
            JSONObject jObj = JSONObject.fromObject(rows.get(i).getColumns(1));
            jArray.add(jObj);
        }
        if(jArray.size()==0){
            log.info("****** done queryTxLogs ******");

            return "no data can be queryed of TableTxLog2";
        }
        log.info("****** done queryTxLogs ******");

        return jArray.toString();
    }
    public TableProto.Row getOwnerOneAsset(ChaincodeStub stub,String owner,String currency){
        log.info("******* in getOwnerOneAsset ******");

        TableProto.Column queryCol =
                TableProto.Column.newBuilder()
                        .setString(owner).build();
        TableProto.Column queryCol1 =
                TableProto.Column.newBuilder()
                        .setString(currency).build();
        List<TableProto.Column> key = new ArrayList<TableProto.Column>();
        key.add(queryCol);
        key.add(queryCol1);
        TableProto.Row row = null;
        try {
            row = stub.getRow(util.getTableAssets(),key);

        }catch(Exception e){
            return null;

        }

        log.info("******* done getOwnerOneAsset ******");
        return row;
    }

    TableProto.Row getCurrencyByID(ChaincodeStub stub,String id){

        TableProto.Column queryCol =
                TableProto.Column.newBuilder()
                        .setString(id).build();
        List<TableProto.Column> key = new ArrayList<TableProto.Column>();
        key.add(queryCol);
        TableProto.Row row = null;

        try {
            row = stub.getRow(util.getTableCurrency(),key);

        }catch(Exception e){
            return null;

        }
        return row;


    }
    public TableProto.Row getLockLog(ChaincodeStub stub,String owner,String currency,String order,boolean islock){
        log.info("******* in getLockLog ******");

        TableProto.Column queryCol =
                TableProto.Column.newBuilder()
                        .setString(owner).build();
        TableProto.Column queryCol1 =
                TableProto.Column.newBuilder()
                        .setString(currency).build();
        TableProto.Column queryCol2 =
                TableProto.Column.newBuilder()
                        .setString(order).build();
        TableProto.Column queryCol3 =
                TableProto.Column.newBuilder()
                        .setBool(islock).build();
        List<TableProto.Column> key = new ArrayList<TableProto.Column>();
        key.add(queryCol);
        key.add(queryCol1);
        key.add(queryCol2);
        key.add(queryCol3);
        TableProto.Row row = null;
        try {
            row = stub.getRow(util.getTableAssetLockLog(),key);

        }catch(Exception e){
            return null;

        }

        log.info("******* done getLockLog ******");

        return row;
    }

    public ArrayList<TableProto.Row> getTXs(ChaincodeStub stub,String owner,
                                            String srcCurrency,String desCurrency,String rawOrder){

        TableProto.Column queryCol =
                TableProto.Column.newBuilder()
                        .setString(owner).build();
        TableProto.Column queryCol1=
                TableProto.Column.newBuilder()
                        .setString(srcCurrency).build();
        TableProto.Column queryCol2 =
                TableProto.Column.newBuilder()
                        .setString(desCurrency).build();
        TableProto.Column queryCol3 =
                TableProto.Column.newBuilder()
                        .setString(rawOrder).build();
        List<TableProto.Column> key = new ArrayList<TableProto.Column>();
        key.add(queryCol);
        key.add(queryCol1);
        key.add(queryCol2);
        key.add(queryCol3);
        ArrayList<TableProto.Row> rows = null;
        try {
            rows = stub.getRows(util.getTableTxLog(),key);
        }catch(Exception e){
            return null;
        }

        return rows;

    }

    public TableProto.Row getTxLogByID(ChaincodeStub stub,String uuid){

        TableProto.Column queryCol =
                TableProto.Column.newBuilder()
                        .setString(uuid).build();
        List<TableProto.Column> key = new ArrayList<TableProto.Column>();
        key.add(queryCol);
        TableProto.Row row = null;
        try {
            row = stub.getRow(util.getTableTxLog2(), key);
        }catch(Exception e){
            return null;
        }

        return row;

    }
    public String queryAssetByOwner(ChaincodeStub stub, String[] args){

        log.info("******* in queryAssetByOwner ******");
        if(args.length!=1){
            return "Incorrect number of aragument. Expecting 1";
        }
        String owner = args[0];
        if(owner==null){
            log.error("queryAssetByOwner error1");
            return "Failed decode owner";
        }
        ArrayList<TableProto.Row> rows = getOwnerAllAsset(stub,owner);
        if(rows==null){
            log.error("queryAssetByOwner error2");
            return null;
        }else if(rows.size()==0){
            log.info("******* done queryAssetByOwner ******");

            return "no data can be queryed";
        }
        JSONArray jArray = new JSONArray();
        for(int i=0;i<rows.size();i++){
            JSONObject jObj = new JSONObject();
            jObj.put("owner",rows.get(i).getColumns(0).getString());
            jObj.put("currency",rows.get(i).getColumns(1).getString());
            jObj.put("count",rows.get(i).getColumns(2).getInt64());
            jObj.put("lockCount",rows.get(i).getColumns(3).getInt64());
            jArray.add(jObj);

        }
        log.info("******* done queryAssetByOwner ******");


        return jArray.toString();
    }
    public String queryMyCurrency(ChaincodeStub stub,String[] args){
        log.info("******* in queryMyCurrency ******");
        if(args.length!=1){
            return "Incorrect number of aragument. Expecting 1";
        }
        String owner = args[0];

        log.info("******* done queryMyCurrency ******");


    }
    public ArrayList<TableProto.Row> getOwnerAllAsset(ChaincodeStub stub, String owner){

        TableProto.Column queryCol =
                TableProto.Column.newBuilder()
                        .setString(owner).build();
        List<TableProto.Column> key = new ArrayList<TableProto.Column>();
        key.add(queryCol);
        ArrayList<TableProto.Row> rows = null;
        try {
            rows = stub.getRows(util.getTableAssets(),key);
        }catch(Exception e){
            return null;
        }
        return rows;

    }
}
