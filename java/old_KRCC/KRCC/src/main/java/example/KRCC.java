package example;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.java.shim.ChaincodeBase;
import org.hyperledger.java.shim.ChaincodeStub;
import org.hyperledger.java.shim.Handler;
import org.hyperledger.protos.TableProto;
import sun.misc.BASE64Decoder;
import org.hyperledger.java.shim.crypto.CryptoPrimitives;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by zerppen on 12/26/16.
 *
 */
public class KRCC extends ChaincodeBase{
    private static final String TableCurrency           = "Currency";
    private static final String TableCurrencyReleaseLog = "CurrencyReleaseLog";
    private static final String TableCurrencyAssignLog  = "CurrencyAssignLog";
    private static final String TableAssets             = "Assets";
    private static final String TableAssetLockLog       = "AssetLockLog";
    private static final String TableTxLog              = "TxLog";
    private static final String TableTxLog2             = "TxLog2";
    private static final String CNY                     = "CNY";
    private static final String USD                     = "USD";
    private static final String CheckErr                = "CheckErr"; // "-1"
    private static final String WorldStateErr           = "WdErr";    //"-2"

    private static  Log log = LogFactory.getLog(KRCC.class);


    @Override
    public String run(ChaincodeStub stub, String function, String[] args) {
        log.info("In run, function:"+function);
        String ret = null;


        if(null==function){
            ret = "Incorrect number of arguments. Expecting 0, No function invoked!";
            return ret;
        }

        switch(function){

            case "Init":
                Init(stub,function,args);
                break;
            case "createCurrency":
                createCurrency(stub,args);
                break;
            case "releaseCurrency":
                releaseCurrency(stub,args);
                break;
            case "assignCurrency":
                assignCurrency(stub,args);
                break;
            case "exchange":
                exchange(stub,args);
                break;
            case "lock":
                lock(stub,args);
                break;
        }
        return ret;
    }

    public String Init(ChaincodeStub stub,String function,String[] args){
        log.info("In Init ...");
        if((args.length)!=0){
            return "Incorrect number of arguments. Expecting 0";
        }
        String retCT = createTable(stub);
        if(null!=retCT){
            log.error("Init error1"+retCT);
            return "Init error1"+retCT;
        }else{
            String retIT =initTable(stub);
            if(null!=retIT){
                return "Init error2"+retIT;
            }
        }
        log.info("Init done");
        return null;
    }

    @Override
    public String query(ChaincodeStub stub, String function, String[] args) {

        String ret = null;

        switch (function){
            case "queryCurrencyByID":
               return queryCurrencyByID(stub, args);
            case "queryAllCurrency":
                return queryAllCurrency(stub, args);
            case "queryTxLogs":
                return queryTxLogs(stub, args);
            case "queryAssetByOwner":
                return queryAssetByOwner(stub, args);
            case "existTable":
                try {
                    return existTable(stub, args);
                }catch(Exception e){
                    return e.toString();
                }
            case "getTable":
                try{
                    return getTable(stub,args);
                }catch(Exception e){
                    return e.toString();
                }

        }

        return ret;
    }

    public  String existTable(ChaincodeStub stub,String[] args) throws Exception{
         if(args.length!=1){
             return "input args is not matched";
         }
        return Boolean.toString(stub.tableExist(args[0]));
    }
    public String getTable(ChaincodeStub stub,String[] args) throws Exception{
        if(args.length!=1){
            return "input args is not matched";
        }
        return stub.getTable(args[0]).toString();
    }
    public String createTable(ChaincodeStub stub){
        List<TableProto.ColumnDefinition> cols = new ArrayList<TableProto.ColumnDefinition>();

        String retStr = null;


        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("ID")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("Count")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.INT64)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("LeftCount")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.INT64)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("Creator")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("CreateTime")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.INT64)
                .build()
        );
        try {
            stub.createTable(TableCurrency,cols);
        }catch (Exception e){
            retStr = e.toString();
            return retStr;
        }

        cols.clear();

        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("Currency")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("Count")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.INT64)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("ReleaseTime")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.INT64)
                .build()
        );
        try {
            stub.createTable(TableCurrencyReleaseLog,cols);
        }catch (Exception e){
            retStr = e.toString();
            return retStr;
        }

        cols.clear();

        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("Currency")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("Owner")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("Count")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.INT64)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("AssignTime")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.INT64)
                .build()
        );
        try {
            stub.createTable(TableCurrencyAssignLog,cols);
        }catch (Exception e){
            retStr = e.toString();
            return retStr;
        }

        cols.clear();

        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("Owner")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("Currency")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("Count")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.INT64)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("LockCount")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.INT64)
                .build()
        );
        try {
            stub.createTable(TableAssets,cols);
        }catch (Exception e){
            retStr = e.toString();
            return retStr;
        }

        cols.clear();

        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("Owner")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("Currency")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("Order")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("IsLock")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.BOOL)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("LockCount")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.INT64)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("LockTime")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.INT64)
                .build()
        );
        try {
            stub.createTable(TableAssetLockLog,cols);
        }catch (Exception e){
            retStr = e.toString();
            return retStr;
        }

        cols.clear();

        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("Owner")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("SrcCurrency")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("DesCurrency")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("RawOrder")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("Detail")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );

        try {
            stub.createTable(TableTxLog,cols);
        }catch (Exception e){
            retStr = e.toString();
            return retStr;
        }

        cols.clear();
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("UUID")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("Detail")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        try {
            stub.createTable(TableTxLog2,cols);
        }catch (Exception e){
            retStr = e.toString();
            return retStr;
        }

        return retStr;
    }

    public String initTable(ChaincodeStub stub){

        String ret = null;
        TableProto.Column col1 = TableProto.Column.newBuilder().setString(CNY).build();
        TableProto.Column col2 = TableProto.Column.newBuilder().setInt64(0).build();
        TableProto.Column col3 = TableProto.Column.newBuilder().setInt64(0).build();
        TableProto.Column col4 = TableProto.Column.newBuilder().setString("system").build();
        TableProto.Column col5 = TableProto.Column.newBuilder().setInt64(System.currentTimeMillis()).build();
        List<TableProto.Column> cols = new ArrayList<TableProto.Column>();
        cols.add(col1);
        cols.add(col2);
        cols.add(col3);
        cols.add(col4);
        cols.add(col5);
        TableProto.Row rows = TableProto.Row.newBuilder()
                .addAllColumns(cols)
                .build();
        try {

            boolean success = false;

            success = stub.insertRow(TableCurrency, rows);

            if (success){
                log.info("Row CNY successfully inserted");
            }
        } catch (Exception e) {
            ret = e.toString();
            return ret;
        }
        col1 = TableProto.Column.newBuilder().setString(USD).build();
        col5 = TableProto.Column.newBuilder().setInt64(System.currentTimeMillis()).build();
        cols.clear();
        cols.add(col1);
        cols.add(col2);
        cols.add(col3);
        cols.add(col4);
        cols.add(col5);
        rows = TableProto.Row.newBuilder()
                .addAllColumns(cols)
                .build();
        try {

            boolean success = false;

            success = stub.insertRow(TableCurrency, rows);

            if (success){
                log.info("Row USD successfully inserted");
            }
        } catch (Exception e) {
            ret = e.toString();
            return ret;
        }


        return ret;
    }


    public String createCurrency(ChaincodeStub stub,String[] args){

        String ret = null;
        log.info("In  createCurrency...");
        if((args.length)!=3){
            ret = "Incorrect number of arguments. Expecting 3,in create currency";
            return ret;
        }
        String id = args[0];
        int count = Integer.parseInt(args[1]);
        String creator = args[2];
        if(creator==null){
            ret = "Failed decodinf creator";
            return ret;
        }
        long timestamp = System.currentTimeMillis();
        TableProto.Column col1 = TableProto.Column.newBuilder().setString(id).build();
        TableProto.Column col2 = TableProto.Column.newBuilder().setInt64(count).build();
        TableProto.Column col3 = TableProto.Column.newBuilder().setInt64(count).build();
        TableProto.Column col4 = TableProto.Column.newBuilder().setString(creator).build();
        TableProto.Column col5 = TableProto.Column.newBuilder().setInt64(timestamp).build();
        List<TableProto.Column> cols = new ArrayList<TableProto.Column>();
        cols.add(col1);
        cols.add(col2);
        cols.add(col3);
        cols.add(col4);
        cols.add(col5);
        TableProto.Row row = TableProto.Row.newBuilder()
                .addAllColumns(cols)
                .build();
        try {

            boolean success = false;

            success = stub.insertRow(TableCurrency, row);

            if (success){
                log.info("create currency successfully inserted");
            }
        } catch (Exception e) {
            ret = e.toString();
            return ret;
        }
        if(count>0){
            cols.clear();
            cols.add(col1);
            cols.add(col2);
            cols.add(col5);
            row = TableProto.Row.newBuilder()
                    .addAllColumns(cols)
                    .build();
            try {

                boolean success = false;

                success = stub.insertRow(TableCurrencyReleaseLog, row);

                if (success){
                    log.info("createCurrency log successfully inserted");
                }
            } catch (Exception e) {
                ret = e.toString();
                return ret;
            }
        }
        log.info("Done  createCurrency...");


        return ret;
    }
    //这里叫增加币更贴切

    public String releaseCurrency(ChaincodeStub stub,String[] args ){
        String ret = null;
        log.info("In  releaseCurrency...");
        if((args.length)!=2){
            ret = "Incorrect number of arguments. Expecting 3,in release currency";
            return ret;
        }
        String id = args[0];
        if(id.equals(CNY)||id.equals(USD)){
            ret = "Currency can't be CNY or USD";
            return ret;
        }
        TableProto.Row row = getTableCurrencRow(stub,id);
        if(row.equals(null)||row==null){
            return "getTableCurrencRow_ERROR";
        }
        String creator = row.getColumns(3).getString();
        if(creator.length()==0&&creator==null){
            ret = "Invalid creator,is null";
            return ret;
        }
/*        boolean verityCreator = isCreator(stub,row.getColumns(3).getString().getBytes());
        if(!verityCreator){
            ret = "Failed checking currency creator identity";
            return ret;
        }
        现在的go版本已经舍弃了  而且java版本执行时会报 SERVER: GET SECURITYCONTEXT FAILED
*/

        long count = Long.parseLong(args[1]);
        if(count<=0){
            ret = "The currency release count must be > 0";
            return ret;
        }

        long timestamp = System.currentTimeMillis();
        long newSumCount = row.getColumns(1).getInt64()+count;
        long newLeftCount = row.getColumns(2).getInt64()+count;

        List<TableProto.Column> cols = new ArrayList<TableProto.Column>();
        cols.add(TableProto.Column.newBuilder().setString(id).build());
        cols.add(TableProto.Column.newBuilder().setInt64(newSumCount).build());
        cols.add(TableProto.Column.newBuilder().setInt64(newLeftCount).build());
        cols.add(TableProto.Column.newBuilder().setString(creator).build());
        cols.add(TableProto.Column.newBuilder().setInt64(timestamp).build());

        TableProto.Row newRow = TableProto.Row.newBuilder()
                .addAllColumns(cols)
                .build();
        try {

            boolean success = stub.replaceRow(TableCurrency, newRow);

            if (success){
                log.info("Failed replacing row");
            }
        } catch (Exception e) {
            ret = e.toString();
            return ret;
        }

        cols.clear();
        cols.add(TableProto.Column.newBuilder().setString(id).build());
        cols.add(TableProto.Column.newBuilder().setInt64(count).build());
        cols.add(TableProto.Column.newBuilder().setInt64(timestamp).build());

        TableProto.Row logRow = TableProto.Row.newBuilder()
                .addAllColumns(cols)
                .build();
        try {

            boolean success = stub.replaceRow(TableCurrencyReleaseLog, logRow);

            if (success){
                log.info("createCurrency log successfully inserted");
            }
        } catch (Exception e) {
            ret = e.toString();
            return ret;
        }

        log.info("Done releaseCurrency...");
        return ret;
    }


    public String assignCurrency(ChaincodeStub stub,String[] args){
        String ret = null;
        log.info("In  Assign Currency...");
        if((args.length)!=1){
            ret = "Incorrect number of arguments. Expecting 3,in release currency";
        }
        String id = null;
        JSONObject jobj = JSONObject.fromObject(args[0]);
        if(jobj.has("currency")&&jobj.get("currency")!=null){
            id = jobj.get("currency").toString();
        }else{
            log.error("error1 can't get assign's currency");
            return "can't get assign's currency";
        }

        JSONArray jarray;
        if(jobj.has("assigns")){
            jarray = jobj.getJSONArray("assigns");
        }else {
            log.error("error2 can't get assign's assigns");
            return "Invalid assign data";
        }


        TableProto.Row row = getTableCurrencRow(stub,id);
        if(row==null||row.equals("")){

            ret = "Faild get row of id:"+id;
            log.error("error3 "+ret);
            return ret;
        }
        String creator = row.getColumns(3).getString();
        long leftCount = row.getColumns(2).getInt64();
        if(creator.length()==0||creator==null){
            ret = "Invalid creator,is null";
            return ret;
        }
/*        boolean verityCreator = isCreator(stub,creator.getBytes());
        if(!verityCreator){
            ret = "Failed checking currency creator identity";
            return ret;
        }
 */       //

        long assignCount = 0;
        for(int j =0;j<jarray.size();j++){

            assignCount += Long.parseLong(jarray.getJSONObject(j).get("count").toString());
        }

        if(assignCount>leftCount){

            ret = "The left count:"+leftCount+" of currency:"+assignCount+
                    " is insufficient";
            log.error("error4: "+ret);
            return ret;
        }

        for(int k=0;k<jarray.size();k++ ){

            String owner = jarray.getJSONObject(k).getString("owner");
            long count = Long.parseLong(jarray.getJSONObject(k).get("count").toString());
            if(owner==null||owner.equals(" ")){
                ret = "Failed decodinfo owner";
                log.error("error5: "+ret);
                return ret;
            }
            if(count<=0)
                continue;
            List<TableProto.Column> cols = new ArrayList<TableProto.Column>();
            cols.add(TableProto.Column.newBuilder().setString(id).build());
            cols.add(TableProto.Column.newBuilder().setString(owner).build());
            cols.add(TableProto.Column.newBuilder().setInt64(count).build());
            cols.add(TableProto.Column.newBuilder().setInt64(System.currentTimeMillis()).build());

            TableProto.Row assignRow = TableProto.Row.newBuilder()
                    .addAllColumns(cols)
                    .build();
            try {

                boolean success = stub.insertRow(TableCurrencyAssignLog, assignRow);

                if (success){
                    log.info("create TableCurrencyAssign log successfully inserted");
                }
            } catch (Exception e) {
                ret = e.toString();
                return ret;
            }

            TableProto.Row assetRow = getOwnerOneAsset(stub,owner,id);
            if(assetRow==null||assetRow.equals("")){
                ret = "Faild get row of id:"+id;
                log.error("error6: "+ret);
                return ret;
            }

            cols.clear();
            cols.add(TableProto.Column.newBuilder().setString(owner).build());
            cols.add(TableProto.Column.newBuilder().setString(id).build());
            cols.add(TableProto.Column.newBuilder().setInt64(count).build());
            cols.add(TableProto.Column.newBuilder().setInt64(0).build());

            TableProto.Row atRow = TableProto.Row.newBuilder()
                    .addAllColumns(cols)
                    .build();
            if(assetRow.getColumnsCount()==0){

                try {

                    boolean success = stub.insertRow(TableAssets, atRow);
                    if (success){
                        log.info("Assign to owner:"+owner+"  successfully inserted");
                    }

                } catch (Exception e) {
                    ret = e.toString();
                    return ret;
                }

            }else{
                try {
                    long asset_count = assetRow.getColumns(2).getInt64();
                    long asset_lockcount = assetRow.getColumns(3).getInt64();
                    cols.clear();
                    cols.add(TableProto.Column.newBuilder().setString(owner).build());
                    cols.add(TableProto.Column.newBuilder().setString(id).build());
                    cols.add(TableProto.Column.newBuilder().setInt64(count+asset_count).build());
                    cols.add(TableProto.Column.newBuilder().setInt64(asset_lockcount).build());
                    TableProto.Row rRow = TableProto.Row.newBuilder()
                            .addAllColumns(cols)
                            .build();

                    boolean success = stub.replaceRow(TableAssets, rRow);
                    if (success){
                        log.info("Assign to owner:"+owner+"  successfully replaced");
                    }

                } catch (Exception e) {
                    ret = e.toString();
                    return ret;
                }

            }

            cols.clear();
            leftCount -= count;
        }

        if(leftCount!=row.getColumns(2).getInt64()){
            List<TableProto.Column> cs = new ArrayList<TableProto.Column>();
            cs.add(TableProto.Column.newBuilder().setString(id).build());
            cs.add(row.getColumns(1));
            cs.add(TableProto.Column.newBuilder().setInt64(leftCount).build());
            cs.add(TableProto.Column.newBuilder().setString(creator).build());
            cs.add(row.getColumns(4));

            TableProto.Row tcRow = TableProto.Row.newBuilder()
                    .addAllColumns(cs)
                    .build();
            try {

                stub.replaceRow(TableCurrency, tcRow);

            } catch (Exception e) {
                ret = e.toString();
                return ret;
            }

        }

        log.info("Done  Assign Currency...");
        return  ret;
    }

    public  String lock(ChaincodeStub stub,String[] args){

        String ret = null;
        log.info("In  lock ...");
        if(args.length!=3){
            return "Incorrect number of arguments. Excepcting 3";
        }
        //String owner,currency,orderId;
        // long count;

        JSONArray jsonArray = JSONArray.fromObject(args[0]);
        boolean islock = Boolean.parseBoolean(args[1]);
        log.info("In  lock .....args[1]="+args[0]+";islock="+islock);
        Map successIn = new HashMap();
        Map failIn    = new HashMap();
        for(int j =0;j<jsonArray.size();j++){
            String owner = jsonArray.getJSONObject(j).getString("owner");
            String currency = jsonArray.getJSONObject(j).getString("currency");
            String orderId = jsonArray.getJSONObject(j).getString("orderId");
            long count = Long.parseLong(jsonArray.getJSONObject(j).getString("count"));


            if(owner==null){
                log.error("lock error2");
                failIn.put(orderId,"Failed decodinf owner");
                continue;
            }
            String ret_loub = lockOrUnlockBalance(stub,owner,currency,orderId,count,islock);
            if(ret_loub!=null){
                if(ret_loub.equals("-1")){
                    failIn.put(orderId,ret_loub);
                    continue;
                }else{
                    log.error("lock error3");
                    return null;
                }

            }
            successIn.put(j,orderId);

        }
        Map allm = new HashMap();
        allm.put("success",successIn);
        allm.put("fail",failIn);
        allm.put("SrcMethod",args[2]);
        byte[] allb = allm.toString().getBytes();

        stub.setEvent("chaincode_lock",allb);
        log.info("done lock...");

        return ret;
    }

    public String exchange(ChaincodeStub stub,String[] args){
        String ret = null;
        log.info("In  Exchange...");
        if(args.length!=1){
            return "Incorrect number of arguments. Exception 2";
        }

        JSONArray jsonArray = JSONArray.fromObject(args[0]);
        if(jsonArray.size()==0||jsonArray==null){
            log.error("exchange error1");
            return "args invalid..";
        }
        Map successIn = new HashMap();
        Map failIn    = new HashMap();
        for(int i = 0;i<jsonArray.size();i++){
            JSONObject buyOrder = JSONObject.fromObject(jsonArray.getJSONObject(i).get("buyOrder"));
            JSONObject sellOrder = JSONObject.fromObject(jsonArray.getJSONObject(i).get("sellOrder"));


/*            String buyOwner = buyOrder.getString("account");
            String sellOwner = sellOrder.getString("account");
            buyOrder.put("account",buyOwner);

            sellOrder.put("account",sellOwner);
*/
            String matchOrder = buyOrder.getString("uuid")+","+sellOrder.getString("uuid");

            if(!buyOrder.getString("srcCurrency").equals(sellOrder.getString("desCurrency"))
                    ||!buyOrder.getString("desCurrency").equals(sellOrder.getString("srcCurrency"))){
                return "The exchange is invalid";
            }

            TableProto.Row buyRow = getTxLogByID(stub,buyOrder.getString("uuid"));
            TableProto.Row sellRow = getTxLogByID(stub,sellOrder.getString("uuid"));

            if(buyRow!=null&&buyRow.getColumnsCount()>0||sellRow!=null&&sellRow.getColumnsCount()>0){
                log.error("exchange error2");
                failIn.put(matchOrder,"exchange error2");

                continue;
            }
            String retETx = execTx(stub,buyOrder,sellOrder);
            if(retETx!=null){
                log.error("exchange error3");
                if(retETx.equals("-1")){
                    failIn.put(matchOrder,"exchange error3");

                    continue;
                }else{
                    log.error("exchange error4");
                    return  ret;
                }
            }
            String retSTL = saveTxLog(stub,buyOrder,sellOrder);
            if(retSTL!=null){
                log.error("exchange error5");
                return ret;
            }
            successIn.put(i,matchOrder);

        }

        Map allm = new HashMap();
        allm.put("success",successIn);
        allm.put("fail",failIn);
        byte []allb = allm.toString().getBytes();

        stub.setEvent("chaincode_exchange",allb);



        return ret;
    }
    public String execTx(ChaincodeStub stub,JSONObject jbuy,JSONObject jsell){

        log.info("in execTx...");
        String ret = null;
        String bAccount = jbuy.getString("account");        //账户
        String bSrcCurrency = jbuy.getString("srcCurrency");//源币种代码
        String bDesCurrency = jbuy.getString("desCurrency");//目标币种代码
        String bRawUUID = jbuy.getString("rawUUID");        //母单UUID
        long bDesCount =jbuy.getLong("desCount");           //目标币种交易数量
        long bSrcCount =jbuy.getLong("srcCount");           //源币种交易数量
        boolean bBuyAll = jbuy.getBoolean("isBuyAll");      //是否买入所有，即为true是以目标币全部兑完为主,否则算部分成交,买完为止；为false则是以源币全部兑完为主,否则算部分成交，卖完为止
        long bExpiredTime = jbuy.getLong("expiredTime");    //超时时间
        long bPendingTime = jbuy.getLong("PendingTime");    //挂单时间
        long bPendedTime = jbuy.getLong("PendedTime");      //挂单完成时间
        long bMatchedTime = jbuy.getLong("matchedTime");    //撮合完成时间
        long bFinishedTime = jbuy.getLong("finishedTime");  //交易完成时间
        String bMetadata = jbuy.getString("metadata");      //存放其他数据，如挂单锁定失败信息
        long bFinalCost = jbuy.getLong("FinalCost");        //源币的最终消耗数量，主要用于买完（IsBuyAll=true）的最后一笔交易计算结余，此时SrcCount有可能大于FinalCost
/*
﻿'{"Args":["exchange","[{\"buyOrder\":{\"uuid\":\"0001\",\"account\":\"Abby\",\"srcCurrency\":\"DEM\",\"desCurrency\":\"EUR\",\"rawUUID\":\"0001\",\"desCount\":\"50\",\"srcCount\":\"600\",\"isBuyAll\":\"true\",\"expiredTime\":\"300\",\"PendingTime\":\"400\",\"PendedTime\":\"10\",\"matchedTime\":\"50\",\"finishedTime\":\"600\",\"metadata\":\"It is a test\",\"FinalCost\":\"40\"},\"sellOrder\":{\"uuid\":\"0002\",\"account\":\"Bill\",\"srcCurrency\":\"EUR\",\"desCurrency\":\"DEM\",\"rawUUID\":\"0002\",\"desCount\":\"40\",\"srcCount\":\"600\",\"isBuyAll\":\"true\",\"expiredTime\":\"300\",\"PendingTime\":\"400\",\"PendedTime\":\"10\",\"matchedTime\":\"50\",\"finishedTime\":\"600\",\"metadata\":\"It is a test\",\"FinalCost\":\"40\"}}]"]}'
*/
        String sAccount = jsell.getString("account");        //账户
        String sSrcCurrency = jsell.getString("srcCurrency");//源币种代码
        String sDesCurrency = jsell.getString("desCurrency");//目标币种代码
        String sRawUUID = jsell.getString("rawUUID");        //母单UUID
        long sDesCount =jsell.getLong("desCount");           //目标币种交易数量
        long sSrcCount =jsell.getLong("srcCount");           //源币种交易数量
        boolean sBuyAll = jsell.getBoolean("isBuyAll");      //是否买入所有，即为true是以目标币全部兑完为主,否则算部分成交,买完为止；为false则是以源币全部兑完为主,否则算部分成交，卖完为止
        long sExpiredTime = jsell.getLong("expiredTime");    //超时时间
        long sPendingTime = jsell.getLong("PendingTime");    //挂单时间
        long sPendedTime = jsell.getLong("PendedTime");      //挂单完成时间
        long sMatchedTime = jsell.getLong("matchedTime");    //撮合完成时间
        long sFinishedTime = jsell.getLong("finishedTime");  //交易完成时间
        String sMetadata = jsell.getString("metadata");      //存放其他数据，如挂单锁定失败信息
        long sFinalCost = jbuy.getLong("FinalCost");        //源币的最终消耗数量，主要用于买完（IsBuyAll=true）的最后一笔交易计算结余，此时SrcCount有可能大于FinalCost

                   // 挂单UUID等于原始ID时表示该单交易完成
        if(bBuyAll && jbuy.get("uuid")==bRawUUID) {

            long lockCount = computeBalance(stub, bAccount, bSrcCurrency,
                    bDesCurrency, bRawUUID, bFinalCost);
            if(lockCount==-1){
                log.error("execTx error1");
                return "-1";
            }

            log.debug("Order " + jbuy.getString("uuid") + " balance " + lockCount);
            if (lockCount > 0) {
                String check = lockOrUnlockBalance(stub, bAccount, bSrcCurrency,
                        bRawUUID, lockCount, false);
                if (check != null) {
                    log.error("execTx error2 "+check);
                    return "Failed unlock balance";
                }

            }
        }
        TableProto.Row buySrcRow = getOwnerOneAsset(stub,bAccount,bSrcCurrency);
        if(buySrcRow==null||buySrcRow.getColumnsCount()==0){
            log.error("execTx error3");
            return "-1 ";
        }

        List<TableProto.Column> cols = new ArrayList<TableProto.Column>();
        cols.add(buySrcRow.getColumns(0));
        cols.add(buySrcRow.getColumns(1));
        cols.add(buySrcRow.getColumns(2));
        cols.add(TableProto.Column.newBuilder().setInt64
                (buySrcRow.getColumns(2).getInt64()-bFinalCost).build());
        TableProto.Row newBuySrcRow = TableProto.Row.newBuilder()
                .addAllColumns(cols)
                .build();
        try {

            boolean success = false;

            success = stub.replaceRow(TableAssets, newBuySrcRow);

            if (success){
                log.info("Row TableAssets successfully replaced");
            }
        } catch (Exception e) {
            ret = e.toString();
            log.error("execTx error4"+ret);
            return "-2";
        }
        TableProto.Row buyDesRow = getOwnerOneAsset(stub,bAccount,bDesCurrency);
        if(buyDesRow==null){
            log.error("execTx error5");
            return "-1 ";
        }

        cols.clear();
        if(buyDesRow.getColumnsCount()==0){
            cols.add(TableProto.Column.newBuilder()
                    .setString(bAccount)
                    .build());
            cols.add(TableProto.Column.newBuilder()
                    .setString(bDesCurrency)
                    .build());
            cols.add(TableProto.Column.newBuilder()
                    .setInt64(bDesCount)
                    .build());
            cols.add(TableProto.Column.newBuilder()
                    .setInt64(0)
                    .build());
            TableProto.Row newBDrow = TableProto.Row.newBuilder()
                    .addAllColumns(cols)
                    .build();
            try {

                boolean success = false;

                success = stub.insertRow(TableAssets, newBDrow);

                if (success){
                    log.info("newBDrow  successfully inserted");
                }
            } catch (Exception e) {
                ret = e.toString();
                log.error("execTx error6 "+ret);
                return "-2";
            }

        }else{
            cols.add(buyDesRow.getColumns(0));
            cols.add(buyDesRow.getColumns(1));
            cols.add(TableProto.Column.newBuilder()
                    .setInt64(buyDesRow.getColumns(2).getInt64()+bDesCount)
                    .build());
            cols.add(buyDesRow.getColumns(3));
            TableProto.Row newBDrow = TableProto.Row.newBuilder()
                    .addAllColumns(cols)
                    .build();
            try {

                boolean success = false;

                success = stub.replaceRow(TableAssets, newBDrow);

                if (success){
                    log.info("newBDrow  successfully replaced");
                }
            } catch (Exception e) {
                ret = e.toString();
                log.error("execTx error7 "+ret);
                return "-2";
            }


        }
        if(sBuyAll && jsell.get("uuid")==sRawUUID){

            long unlockCount = computeBalance(stub,sAccount,sSrcCurrency,
                    sDesCurrency,sRawUUID,sFinalCost);
            if(unlockCount>0){
                String check = lockOrUnlockBalance(stub,sAccount,sSrcCurrency,
                        sRawUUID,unlockCount,false);
                log.debug("Order "+jsell.getString("uuid")+" balance "+unlockCount);
                if(check!=null){
                    log.error("execTx error9");
                    return "-1";
                }

            }

        }

        TableProto.Row sellSrcRow = getOwnerOneAsset(stub,sAccount,sSrcCurrency);
        if(sellSrcRow==null||sellSrcRow.getColumnsCount()==0){
            log.error("execTx error10");
            return "the user have not currency "+sSrcCurrency;
        }
        cols.clear();
        cols.add(sellSrcRow.getColumns(0));
        cols.add(sellSrcRow.getColumns(1));
        cols.add(sellSrcRow.getColumns(2));
        cols.add(TableProto.Column.newBuilder().setInt64
                (sellSrcRow.getColumns(2).getInt64()-sFinalCost).build());
        TableProto.Row newSellSrcRow = TableProto.Row.newBuilder()
                .addAllColumns(cols)
                .build();
        try {

            boolean success = false;
            success = stub.replaceRow(TableAssets, newSellSrcRow);

            if (success){
                log.info("Row TableAssets successfully replaced");
            }
        } catch (Exception e) {
            ret = e.toString();
            log.error("execTx error11"+ret);
            return ret;
        }

        TableProto.Row sellDesRow = getOwnerOneAsset(stub,sAccount,sDesCurrency);
        if(sellDesRow==null){
            log.error("execTx error12");
            return "Faild retrieving asset "+sDesCurrency;
        }
        cols.clear();
        if(sellDesRow.getColumnsCount()==0){
            cols.add(TableProto.Column.newBuilder()
                    .setString(sAccount)
                    .build());
            cols.add(TableProto.Column.newBuilder()
                    .setString(sDesCurrency)
                    .build());
            cols.add(TableProto.Column.newBuilder()
                    .setInt64(sDesCount)
                    .build());
            cols.add(TableProto.Column.newBuilder()
                    .setInt64(0)
                    .build());
            TableProto.Row newSDrow = TableProto.Row.newBuilder()
                    .addAllColumns(cols)
                    .build();
            try {

                boolean success = stub.insertRow(TableAssets, newSDrow);

                if (success){
                    log.info("newSDrow  successfully inserted");
                }
            } catch (Exception e) {
                ret = e.toString();
                log.error("execTx error13 "+ret);
                return ret;
            }

        }else{
            cols.add(sellDesRow.getColumns(0));
            cols.add(sellDesRow.getColumns(1));
            cols.add(TableProto.Column.newBuilder()
                    .setInt64(sellDesRow.getColumns(2).getInt64()+sDesCount)
                    .build());
            cols.add(sellDesRow.getColumns(3));
            TableProto.Row newSDrow = TableProto.Row.newBuilder()
                    .addAllColumns(cols)
                    .build();
            try {

                boolean success = stub.replaceRow(TableAssets, newSDrow);

                if (success){
                    log.info("newBDrow  successfully replaced");
                }
            } catch (Exception e) {
                ret = e.toString();
                log.error("execTx error7 "+ret);
                return ret;
            }

        }

        return ret;
    }

    public String saveTxLog(ChaincodeStub stub,JSONObject jbuy,JSONObject jsell){
        String ret = null;

        List<TableProto.Column> cols = new ArrayList<TableProto.Column>();
        cols.add(TableProto.Column.newBuilder()
                .setString(jbuy.getString("account"))
                .build());
        cols.add(TableProto.Column.newBuilder()
                .setString(jbuy.getString("srcCurrency"))
                .build());
        cols.add(TableProto.Column.newBuilder()
                .setString(jbuy.getString("desCurrency"))
                .build());
        cols.add(TableProto.Column.newBuilder()
                .setString(jbuy.getString("rawUUID"))
                .build());
        cols.add(TableProto.Column.newBuilder()
                .setString(jbuy.toString())
                .build());
        TableProto.Row row = TableProto.Row.newBuilder()
                .addAllColumns(cols)
                .build();
        try {

            boolean success = stub.insertRow(TableTxLog, row);

            if (success){
                log.info("TableTxLog ROW  successfully inserted");
            }
        } catch (Exception e) {
            ret = e.toString();
            log.error("saveTxLog error1 "+ret);
            return ret;
        }
        cols.clear();
        cols.add(TableProto.Column.newBuilder()
                .setString(jbuy.getString("uuid"))
                .build());
        cols.add(TableProto.Column.newBuilder()
                .setString(jbuy.toString())
                .build());
        row = TableProto.Row.newBuilder()
                .addAllColumns(cols)
                .build();
        try {

            boolean success = stub.insertRow(TableTxLog2, row);

            if (success){
                log.info("TableTxLog2 ROW  successfully inserted");
            }
        } catch (Exception e) {
            ret = e.toString();
            log.error("saveTxLog error2 "+ret);
            return ret;
        }
        cols.clear();
        cols.add(TableProto.Column.newBuilder()
                .setString(jsell.getString("account"))
                .build());
        cols.add(TableProto.Column.newBuilder()
                .setString(jsell.getString("srcCurrency"))
                .build());
        cols.add(TableProto.Column.newBuilder()
                .setString(jsell.getString("desCurrency"))
                .build());
        cols.add(TableProto.Column.newBuilder()
                .setString(jsell.getString("rawUUID"))
                .build());
        cols.add(TableProto.Column.newBuilder()
                .setString(jsell.toString())
                .build());
        row = TableProto.Row.newBuilder()
                .addAllColumns(cols)
                .build();
        try {

            boolean success = stub.insertRow(TableTxLog, row);

            if (success){
                log.info("TableTxLog ROW  successfully inserted");
            }
        } catch (Exception e) {
            ret = e.toString();
            log.error("saveTxLog error3 "+ret);
            return ret;
        }

        cols.clear();
        cols.add(TableProto.Column.newBuilder()
                .setString(jsell.getString("uuid"))
                .build());
        cols.add(TableProto.Column.newBuilder()
                .setString(jsell.toString())
                .build());
        row = TableProto.Row.newBuilder()
                .addAllColumns(cols)
                .build();
        try {

            boolean success = stub.insertRow(TableTxLog2, row);

            if (success){
                log.info("TableTxLog2 ROW  successfully inserted");
            }
        } catch (Exception e) {
            ret = e.toString();
            log.error("saveTxLog error4 "+ret);
            return ret;
        }

        return ret;
    }

    public long computeBalance(ChaincodeStub stub,String owner,String srcCurrency,
                               String desCurrency,String rawUUID,long currentCost){
        log.info("in computeBalance...");
        long ret=0;
        TableProto.Row logRow = getLockLog(stub,owner,srcCurrency,rawUUID,true);
        if(logRow==null||logRow.getColumnsCount()==0){
            log.error("get locklog faild");
            return -1;
        }
        long sumcost = 0;
        synchronized (this){
            ArrayList<TableProto.Row> txRows = getTXs(stub,owner,srcCurrency,desCurrency,rawUUID);
            for(TableProto.Row row:txRows){
                JSONObject jobj = JSONObject.fromObject(row.getColumns(4).getString());
                sumcost += Long.parseLong(jobj.get("FinalCost").toString());
            }

        }

        long lockCount = logRow.getColumns(4).getInt64();
        return lockCount-sumcost-currentCost;
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
            rows = stub.getRows(TableTxLog,key);
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
            row = stub.getRow(TableTxLog2, key);
        }catch(Exception e){
            return null;
        }

        return row;

    }


    public  String lockBalance(ChaincodeStub stub,String[] args){

        String ret = null;
        log.info("In  lock Currency...");
        if(args.length!=3){
            return "Incorrect number of arguments. Excepcting 3";
        }
        String owner = getFromBASE64(args[0].toString());
        if(owner==null||owner.equals("")){
            return "Failed decode owner";
        }
        String id = args[1].toString();
        long count = Long.parseLong(args[2].toString());
        String order = args[3].toString();
        if(count<=0){
            return "count:"+count+" is invaild";
        }
        log.info("Done  lock Currency...");

        return ret;
    }

    public  String unlockBalance(ChaincodeStub stub,String[] args){

        String ret = null;
        log.info("In  unlock Currency...");
        if(args.length!=3){
            return "Incorrect number of arguments. Excepcting 3";
        }
        String owner = getFromBASE64(args[0].toString());
        if(owner==null||owner.equals("")){
            return "Failed decode owner";
        }
        String id = args[1].toString();
        long count = Long.parseLong(args[2].toString())*(-1);
        String order = args[3].toString();

        if(count>=0){
            return "count:"+count+" is invaild";
        }
        log.info("Done  unlock Currency...");

        return ret;
    }

    public  String lockOrUnlockBalance(ChaincodeStub stub, String owner,String currency,
                                       String order,long count,boolean islock){
        log.info("in lockOrUnlockBalance...");
        String ret = null;
        TableProto.Row row = getOwnerOneAsset(stub,owner,currency);
        if(row==null||row.equals("")||row.getColumnsCount()==0){
            log.error( "lockOrUnlockBalance error1 ;"+ "Faild get row of id:"+currency);
            return "-1";
        }
        log.info("getOwnerOneAsset row:"+row.toString());

        long currencyCount = row.getColumns(2).getInt64();
        long currencyLockCount = row.getColumns(3).getInt64();
        log.info("Have count:"+currencyCount+"; have lockCount:"+currencyLockCount+" ;islock:"+islock);
        if((islock && (currencyCount<count?true:false))||(!islock && (currencyLockCount<count?true:false))){
            log.error("Currency  or locked Currency " +currency+ "  of the user is insufficient ");
            return "-1";
        }

        TableProto.Row lockRow = getLockLog(stub,owner,currency,order,islock);
        if(lockRow !=null && lockRow.getColumnsCount()>0){
            log.error("lockOrUnlockBalance error2");
            return " -1";
        }

        List<TableProto.Column> cs = new ArrayList<TableProto.Column>();
        cs.add(TableProto.Column.newBuilder().setString(owner).build());
        cs.add(TableProto.Column.newBuilder().setString(currency).build());

        if ( islock ){

            cs.add(TableProto.Column.newBuilder().setInt64(currencyCount-count).build());
            cs.add(TableProto.Column.newBuilder().setInt64(currencyLockCount+count).build());
        }else{

            cs.add(TableProto.Column.newBuilder().setInt64(currencyCount+count).build());
            cs.add(TableProto.Column.newBuilder().setInt64(currencyLockCount-count).build());
        }


        TableProto.Row taRow = TableProto.Row.newBuilder()
                .addAllColumns(cs)
                .build();
        try {

            stub.replaceRow(TableAssets, taRow);

        } catch (Exception e) {
            log.error( "lockOrUnlockBalance error3 "+e.toString());
            return "-2";
        }

        cs.clear();
        cs.add(TableProto.Column.newBuilder().setString(owner).build());
        cs.add(TableProto.Column.newBuilder().setString(currency).build());
        cs.add(TableProto.Column.newBuilder().setString(order).build());
        cs.add(TableProto.Column.newBuilder().setBool(islock).build());
        cs.add(TableProto.Column.newBuilder().setInt64(count).build());
        cs.add(TableProto.Column.newBuilder().setInt64(System.currentTimeMillis()).build());

        TableProto.Row tallRow = TableProto.Row.newBuilder()
                .addAllColumns(cs)
                .build();
        try {

            stub.replaceRow(TableAssetLockLog, tallRow);

        } catch (Exception e) {
            log.error( "lockOrUnlockBalance error4 "+e.toString());
            return "-2";
        }

        return ret;
    }




    /*
     // In order to enforce access control, we require that the
	// metadata contains the following items:
	// 1. a certificate Cert
	// 2. a signature Sigma under the signing key corresponding
	// to the verification key inside Cert of :
	// (a) Cert;
	// (b) The payload of the transaction (namely, function name and args) and
	// (c) the transaction binding.

	// Verify Sigma=Sign(certificate.sk, Cert||tx.Payload||tx.Binding) against Cert.vk
     */
    public boolean isCreator(ChaincodeStub stub, byte[] certificate){

        log.info("in isCreator....");
        byte[] sigma = stub.getCallerMetadata();
        byte[] payload = stub.getPayload();
        // byte[] bingding = stub.getBinding();

        if(sigma.length==0||certificate.length==0||payload.length==0){
            log.error("get securitycontext failed");
            return false;

        }
        boolean verity = stub.verifySignature(certificate,sigma,payload);

        if(!verity){
            log.error("invalid signature");
        }

        return verity;
    }


    public TableProto.Row getTableCurrencRow(ChaincodeStub stub,String id){
        TableProto.Column queryCol =
                TableProto.Column.newBuilder()
                        .setString(id).build();
        List<TableProto.Column> key = new ArrayList<TableProto.Column>();
        key.add(queryCol);
        TableProto.Row row = null;
        try {
            row = stub.getRow(TableCurrency, key);
        }catch(Exception e){
            return null;
        }

        return row;
    }

    public TableProto.Row getOwnerOneAsset(ChaincodeStub stub,String owner,String currency){
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
            row = stub.getRow(TableAssets,key);

        }catch(Exception e){
            return null;

        }

        return row;
    }

    public TableProto.Row getLockLog(ChaincodeStub stub,String owner,String currency,String order,boolean islock){
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
            row = stub.getRow(TableAssetLockLog,key);

        }catch(Exception e){
            return null;

        }

        return row;
    }





    public  String getFromBASE64(String s) {
        if (s == null) return null;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] b = decoder.decodeBuffer(s);
            return new String(b);
        } catch (Exception e) {
            return null;
        }
    }





    public String queryCurrencyByID(ChaincodeStub stub,String[] args){

        log.debug("queryCurrencyByID...");
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

        return cuJson.toString();
    }
    TableProto.Row getCurrencyByID(ChaincodeStub stub,String id){

        TableProto.Column queryCol =
                TableProto.Column.newBuilder()
                        .setString(id).build();
        List<TableProto.Column> key = new ArrayList<TableProto.Column>();
        key.add(queryCol);
        TableProto.Row row = null;

        try {
            row = stub.getRow(TableCurrency,key);

        }catch(Exception e){
            return null;

        }
        return row;


    }
    public String queryAllCurrency(ChaincodeStub stub,String[] args){

        log.debug("queryAllCurrency...");
        if(args.length!=0){
            log.error("incorrect number of arguments");
            return "incorrect number of arguments";
        }
        ArrayList<TableProto.Row> rows = null;
        try {

            List<TableProto.Column> key = new ArrayList<TableProto.Column>();
            rows = stub.getRows(TableCurrency,key);
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
            return "no data can be queryed";
        }

        return jArray.toString();
    }
    public String queryTxLogs(ChaincodeStub stub,String[] args){

        log.debug("queryTxLogs...");
        if(args.length!=0){
            return "Incorrect number of arguments";
        }
        ArrayList<TableProto.Row> rows = null;
        try {

            List<TableProto.Column> key = new ArrayList<TableProto.Column>();


            rows = stub.getRows(TableTxLog2,key);
        }catch(Exception e){
            return "getRows operation failed";
        }
        JSONArray jArray = new JSONArray();
        for(int i=0;i<rows.size();i++){
            JSONObject jObj = JSONObject.fromObject(rows.get(i).getColumns(1));
            jArray.add(jObj);
        }
        if(jArray.size()==0){
            return "no data can be queryed of TableTxLog2";
        }
        return null;
    }

    public String queryAssetByOwner(ChaincodeStub stub,String[] args){

        log.debug("queryAssetByOwner...");
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

        return jArray.toString();
    }
    public ArrayList<TableProto.Row> getOwnerAllAsset(ChaincodeStub stub,String owner){

        TableProto.Column queryCol =
                TableProto.Column.newBuilder()
                        .setString(owner).build();
        List<TableProto.Column> key = new ArrayList<TableProto.Column>();
        key.add(queryCol);
        ArrayList<TableProto.Row> rows = null;
        try {
            rows = stub.getRows(TableAssets,key);
        }catch(Exception e){
            return null;
        }
        return rows;

    }


    @Override
    public String getChaincodeID() {
        return null;
    }

    public static void main(String[] args) throws Exception {

        new CryptoPrimitives(256,"SHA3");
        System.out.println("Hello world! starting "+args);
        log.info("starting");
        new KRCC().start(args);
    }
}
