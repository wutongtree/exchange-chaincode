package krcc;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.java.shim.ChaincodeStub;
import org.hyperledger.protos.TableProto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zerppen on 3/19/17.
 */
public class run {
    private static Log log = LogFactory.getLog(run.class);
    private static query qy = new query();


    public String initAccount(ChaincodeStub stub,String []args){
        log.info("****** in initAccount ******");
        String ret = null;
        if(args.length!=1){
            ret = "*** input args' length is wrong ***";
            log.error(ret);
            return ret;
        }

        TableProto.Row row = qy.getOwnerOneAsset(stub,args[0],util.getCNY());

        if(util.checkRow(row)==0){

            String insertCNY = insertAsset(stub,args[0],util.getCNY(),0);
            if(insertCNY!=null){
                ret = "error1:"+insertCNY;
                log.error(ret);
                return ret;
            }

        }else if(util.checkRow(row)==-1){
            log.info("*** initAccount error2: get user:"+args[0]+"'s Asset Row wrong");
            return ret;
        }

        row = qy.getOwnerOneAsset(stub,args[0],util.getCNY());

        if(util.checkRow(row)==0){

            String insertUSD = insertAsset(stub,args[0],util.getCNY(),0);
            if(insertUSD!=null){
                ret = "error3:"+insertUSD;
                log.error(ret);
                return ret;
            }
        }else if(util.checkRow(row)==-1){
            log.info("*** initAccount error4: get user:"+args[0]+"'s Asset Row wrong");
            return ret;
        }
        log.info("****** DONE initAccount ******");

        return ret;
    }
    public String create(ChaincodeStub stub,String[] args){
        log.info("**** In  create ***");

        String ret = null;
        if((args.length)!=3){
            ret = "Incorrect number of arguments. Expecting 3";
            return ret;
        }
        String id = args[0];
        long count = Long.parseLong(args[1]);
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

            boolean success = stub.insertRow(util.getTableCurrency(), row);

            if (success){
                log.info("create currency successfully inserted");
            }
        } catch (Exception e) {
            ret = e.toString();
            log.error(ret);
            return ret;
        }
        if(count>0){
            String save = saveReleaseLog(stub,id,count,timestamp);

        }
        log.info("Done  createCurrency...");


        return ret;
    }
    public String release(ChaincodeStub stub,String[] args ){
        log.info("*** In  release ***");
        String ret = null;

        if((args.length)!=2){
            ret = "Incorrect number of arguments. Expecting 3";
            return ret;
        }
        String id = args[0];
        if(id.equals(util.getCNY())||id.equals(util.getUSD())){
            ret = "Currency can't be CNY or USD";
            log.error(ret);
            return ret;
        }


        TableProto.Row row = qy.getCurrencyByID(stub,id);
        if(util.checkRow(row)==-1){
            ret = "error1:get id:"+id+"'s currency row wrong";
            log.error(ret);
            return ret;
        }else if(util.checkRow(row)==0){
            ret = "error2: Cant't find id:"+id+"'s currency";
            return ret;
        }
        String creator = row.getColumns(3).getString();


        long count = Long.parseLong(args[1]);
        if(count<=0){
            ret = "The currency release count must be > 0";
            log.error(ret);
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

            boolean success = stub.replaceRow(util.getTableCurrency(), newRow);

            if (success){
                log.info("currency release replacing row");
            }
        } catch (Exception e) {
            ret = e.toString();
            log.error(ret);
            return ret;
        }

         saveReleaseLog(stub,id,count,timestamp);
        log.info("Done releaseCurrency...");
        return ret;
    }

    public String assign(ChaincodeStub stub,String[] args){
        String ret = null;
        log.info("*** In  Assign Currency ***");
        if((args.length)!=1){
            ret = "Incorrect number of arguments. Expecting 3,in release currency";
        }
        String currency = null;
        JSONObject jobj = JSONObject.fromObject(args[0]);
        if(jobj.has("currency")&&jobj.get("currency")!=null){
            currency = jobj.get("currency").toString();
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


        TableProto.Row row = qy.getCurrencyByID(stub,currency);
        if(util.checkRow(row)==-1){
            ret = "error3:get id:"+currency+"'s currency row wrong";
            log.error(ret);
            return ret;
        }else if(util.checkRow(row)==0){
            ret = "error4: Cant't find id:"+currency+"'s currency";
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

            long inCount = Long.parseLong(jarray.getJSONObject(j).get("count").toString());
            if(inCount<=0)
                continue;

            assignCount += inCount;
            if(assignCount>leftCount){

                ret = "The left count:"+leftCount+" of currency:"+assignCount+
                        " is insufficient";
                log.error("error4: "+ret);
                return ret;
            }
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
            String save = saveAssignLog(stub,currency,owner,count);

            if(save!=null){
                ret = "error 6:"+save;
            }

            TableProto.Row assetRow = qy.getOwnerOneAsset(stub,owner,currency);

            if(util.checkRow(assetRow)==-1){
                ret = "Faild get row of id:"+currency;
                log.error("error6: "+ret);
                return ret;
            }else if(util.checkRow(assetRow)==0){
                String insert = insertAsset(stub,owner,currency,count);
                if(insert!=null){
                    ret = "in when k="+k+"error7:"+insert;
                    log.error(ret);
                    return ret;
                }
            } else{

                    long asset_count = assetRow.getColumns(2).getInt64();
                    long asset_lockcount = assetRow.getColumns(3).getInt64();

                    String replace = replaceAsset(stub,owner,currency,count+asset_count,asset_lockcount);
                    if(replace!=null){
                        ret = "in when k="+k+"error8:"+replace;
                        log.error(ret);
                        return ret;
                    }
            }

            leftCount -= count;
        }

        if(leftCount!=row.getColumns(2).getInt64()){
            List<TableProto.Column> cs = new ArrayList<TableProto.Column>();
            cs.add(TableProto.Column.newBuilder().setString(currency).build());
            cs.add(row.getColumns(1));
            cs.add(TableProto.Column.newBuilder().setInt64(leftCount).build());
            cs.add(TableProto.Column.newBuilder().setString(creator).build());
            cs.add(row.getColumns(4));

            TableProto.Row tcRow = TableProto.Row.newBuilder()
                    .addAllColumns(cs)
                    .build();
            try {

                boolean success = stub.replaceRow(util.getTableCurrency(), tcRow);
                if (success){
                    log.info("Currency's leftCount  successfully replaced");
                }

            } catch (Exception e) {
                ret = ("error8:"+e.toString());
                log.error(ret);
                return ret;
            }

        }

        log.info("****** Done  Assign Currency ******");
        return  ret;
    }

    public  String lock(ChaincodeStub stub,String[] args){

        String ret = null;
        log.info("****** In  lock ******");
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
        log.info("****** Done  lock ******");

        return ret;
    }
    public  String lockOrUnlockBalance(ChaincodeStub stub, String owner,String currency,
                                       String order,long count,boolean islock){
        log.info("*** in lockOrUnlockBalance ***");
        String ret = null;
        TableProto.Row row = qy.getOwnerOneAsset(stub,owner,currency);
        if(util.checkRow(row)!=-2){
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

        TableProto.Row lockRow = qy.getLockLog(stub,owner,currency,order,islock);
        if(util.checkRow(lockRow)==-2){
            log.error("lockOrUnlockBalance error2");
            return " -1";
        }

        if ( islock){

            currencyCount -= count;
            currencyLockCount += count;
        }else{

            currencyCount += count;
            currencyLockCount -= count;
        }


        String replace = replaceAsset(stub,owner,currency,currencyCount,currencyLockCount);
        if(replace!=null){
            log.error( "lockOrUnlockBalance error3 "+replace);
            return "-2";
        }
        List<TableProto.Column> cs = new ArrayList<TableProto.Column>();

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

            stub.replaceRow(util.getTableAssetLockLog(), tallRow);

        } catch (Exception e) {
            log.error( "lockOrUnlockBalance error4 "+e.toString());
            return "-2";
        }
        log.info("*** done lockOrUnlockBalance ***");

        return ret;
    }
    public String exchange(ChaincodeStub stub,String[] args){
        String ret = null;
        log.info("****** In  Exchange ******");
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

            String matchOrder = buyOrder.getString("uuid")+","+sellOrder.getString("uuid");

            if(!buyOrder.getString("srcCurrency").equals(sellOrder.getString("desCurrency"))
                    ||!buyOrder.getString("desCurrency").equals(sellOrder.getString("srcCurrency"))){
                ret = "The exchange is invalid";
                log.error(ret);
                return ret;
            }

            TableProto.Row buyRow = qy.getTxLogByID(stub,buyOrder.getString("uuid"));
            TableProto.Row sellRow = qy.getTxLogByID(stub,sellOrder.getString("uuid"));

            if(util.checkRow(buyRow)==-2||util.checkRow(sellRow)==-2){
                log.error("exchange error2");
                failIn.put(matchOrder,"exchange error2");

                continue;
            }
            if(util.checkRow(buyRow)==-1||util.checkRow(sellRow)==-1){
                log.error("exchange error3");
                failIn.put(matchOrder,"exchange error3");

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

        log.info("****** in execTx ******");
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

            log.info("Order " + jbuy.getString("uuid") + " balance " + lockCount);
            if (lockCount > 0) {
                String check = lockOrUnlockBalance(stub, bAccount, bSrcCurrency,
                        bRawUUID, lockCount, false);
                if (check != null) {
                    log.error("execTx error2 "+check);
                    return "Failed unlock balance";
                }

            }
        }
        TableProto.Row buySrcRow = qy.getOwnerOneAsset(stub,bAccount,bSrcCurrency);
        if(util.checkRow(buySrcRow)!=-2){
            log.error("execTx error3");
            return "-1 ";
        }


        String replaceBuySrc = replaceAsset(stub,buySrcRow.getColumns(0).getString(),
                buySrcRow.getColumns(1).getString(),buySrcRow.getColumns(2).getInt64(),
                buySrcRow.getColumns(3).getInt64()-bFinalCost);
        if(replaceBuySrc!=null){
            ret = "replace owner:"+buySrcRow.getColumns(0).getString()+"'s currency:"+buySrcRow.getColumns(2).getInt64()+" failed";
            log.error(ret);
            return ret;
        }

        TableProto.Row buyDesRow = qy.getOwnerOneAsset(stub,bAccount,bDesCurrency);
        if(buyDesRow==null){
            log.error("execTx error5");
            return "-1 ";
        }

        if(buyDesRow.getColumnsCount()==0){

                String insertBuyDes = insertAsset(stub,bAccount,bDesCurrency,bDesCount);
                if(insertBuyDes!=null){
                    ret = "insert owner:"+bAccount+"'s currency:"+bDesCurrency+" failed";
                    log.error(ret);
                    return ret;
                }
        }else{

            String replaceBuyDes = replaceAsset(stub,buyDesRow.getColumns(0).getString(),
                    buyDesRow.getColumns(1).getString(),buyDesRow.getColumns(2).getInt64()+bDesCount,
                    buyDesRow.getColumns(3).getInt64());
            if(replaceBuyDes!=null){
                ret = "replace owner:"+buyDesRow.getColumns(0).getString()+"'s currency:"+buySrcRow.getColumns(2).getInt64()+" failed";
                log.error(ret);
                return ret;
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

        TableProto.Row sellSrcRow = qy.getOwnerOneAsset(stub,sAccount,sSrcCurrency);
        if(util.checkRow(sellSrcRow)!=-2){
            log.error("execTx error10");
            return "the user have not currency "+sSrcCurrency;
        }


        String replaceSellSrc = replaceAsset(stub,sellSrcRow.getColumns(0).getString(),
                sellSrcRow.getColumns(1).getString(),sellSrcRow.getColumns(2).getInt64(),
                sellSrcRow.getColumns(3).getInt64()-sFinalCost);
        if(replaceSellSrc!=null){
            ret = "replace owner:"+sellSrcRow.getColumns(0).getString()+"'s currency:"+sellSrcRow.getColumns(2).getInt64()+" failed";
            log.error(ret);
            return ret;
        }



        TableProto.Row sellDesRow =  qy.getOwnerOneAsset(stub,sAccount,sDesCurrency);
        if(sellDesRow==null){
            log.error("execTx error12");
            return "Faild retrieving asset "+sDesCurrency;
        }

        if(sellDesRow.getColumnsCount()==0){

            String insertSellDes = insertAsset(stub,sAccount,sDesCurrency,sDesCount);
            if(insertSellDes!=null){
                ret = "insert owner:"+sAccount+"'s currency:"+sDesCurrency+" failed";
                log.error(ret);
                return ret;
            }

        }else{

            String replacesellDes = replaceAsset(stub,sellDesRow.getColumns(0).getString(),
                    sellDesRow.getColumns(1).getString(),sellDesRow.getColumns(2).getInt64()+sDesCount,
                    buyDesRow.getColumns(3).getInt64());
            if(replacesellDes!=null){
                ret = "replace owner:"+sellDesRow.getColumns(0).getString()+"'s currency:"+sellDesRow.getColumns(2).getInt64()+" failed";
                log.error(ret);
                return ret;
            }


        }
        log.info("****** done execTx ******");


        return ret;
    }

    public String saveTxLog(ChaincodeStub stub,JSONObject jbuy,JSONObject jsell){
        log.info("****** in saveTxLog ******");

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

            boolean success = stub.insertRow(util.getTableTxLog(), row);

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

            boolean success = stub.insertRow(util.getTableTxLog2(), row);

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

            boolean success = stub.insertRow(util.getTableTxLog(), row);

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

            boolean success = stub.insertRow(util.getTableTxLog2(), row);

            if (success){
                log.info("TableTxLog2 ROW  successfully inserted");
            }
        } catch (Exception e) {
            ret = e.toString();
            log.error("saveTxLog error4 "+ret);
            return ret;
        }
        log.info("****** done saveTxLog ******");


        return ret;
    }

    public long computeBalance(ChaincodeStub stub,String owner,String srcCurrency,
                               String desCurrency,String rawUUID,long currentCost){
        log.info("****** in computeBalance ******");
        long ret=0;
        TableProto.Row logRow =     qy.getLockLog(stub,owner,srcCurrency,rawUUID,true);
        if(util.checkRow(logRow)!=-2){
            log.error("get locklog faild");
            return -1;
        }
        long sumcost = 0;
        synchronized (this){
            ArrayList<TableProto.Row> txRows = qy.getTXs(stub,owner,srcCurrency,desCurrency,rawUUID);
            for(TableProto.Row row:txRows){
                JSONObject jobj = JSONObject.fromObject(row.getColumns(4).getString());
                sumcost += Long.parseLong(jobj.get("FinalCost").toString());
            }

        }

        long lockCount = logRow.getColumns(4).getInt64();
        log.info("****** done computeBalance ******");

        return lockCount-sumcost-currentCost;
    }



    private String saveReleaseLog(ChaincodeStub stub,String id,long count,long time){
        log.info("*** in saveReleaseLog ***");
        String ret = null;
        List<TableProto.Column> cols = new ArrayList<TableProto.Column>();
        cols.add(TableProto.Column.newBuilder().setString(id).build());
        cols.add(TableProto.Column.newBuilder().setInt64(count).build());
        cols.add(TableProto.Column.newBuilder().setInt64(time).build());
        TableProto.Row row = TableProto.Row.newBuilder()
                .addAllColumns(cols)
                .build();
        try {

            boolean success = stub.insertRow(util.getTableCurrencyReleaseLog(), row);

            if (success){
                log.info("saveReleaseLog Row successfully inserted");
            }
        } catch (Exception e) {
            ret = e.toString();
            log.error(ret);
            return ret;
        }

        log.info("*** done saveReleaseLog ***");
        return ret;

    }

    private String saveAssignLog(ChaincodeStub stub,String id,String reciver,long count){
        log.info("*** in saveAssignLog ***");
        String ret = null;

        List<TableProto.Column> cols = new ArrayList<TableProto.Column>();
        cols.add(TableProto.Column.newBuilder().setString(id).build());
        cols.add(TableProto.Column.newBuilder().setString(reciver).build());
        cols.add(TableProto.Column.newBuilder().setInt64(count).build());
        cols.add(TableProto.Column.newBuilder().setInt64(System.currentTimeMillis()).build());

        TableProto.Row assignRow = TableProto.Row.newBuilder()
                .addAllColumns(cols)
                .build();
        try {

            boolean success = stub.insertRow(util.getTableCurrencyAssignLog(), assignRow);

            if (success){
                log.info("create TableCurrencyAssign log successfully inserted");
            }
        } catch (Exception e) {
            ret = e.toString();
            return ret;
        }
        log.info("*** done saveReleaseLog ***");
        return ret;
    }
    private String insertAsset(ChaincodeStub stub,String owner,String currency,long count){
        log.info("*** in insertAsset ***");

        String ret = null;
        List<TableProto.Column> cols = new ArrayList<TableProto.Column>();
        cols.add(TableProto.Column.newBuilder().setString(owner).build());
        cols.add(TableProto.Column.newBuilder().setString(currency).build());
        cols.add(TableProto.Column.newBuilder().setInt64(count).build());
        cols.add(TableProto.Column.newBuilder().setInt64(0).build());
        try {

            TableProto.Row atRow = TableProto.Row.newBuilder()
                    .addAllColumns(cols)
                    .build();
            boolean success = stub.insertRow(util.getTableAssets(), atRow);
            if (success){
                log.info("Assign to owner:"+owner+"  successfully inserted");
            }

        } catch (Exception e) {
            ret = e.toString();
            return ret;
        }
        log.info("*** Done insertAsset ***");

        return ret;

    }
    private String replaceAsset(ChaincodeStub stub,String owner,String currency,long count,long leftCount){
        log.info("*** in replaceAsset ***");

        String ret = null;
        List<TableProto.Column> cols = new ArrayList<TableProto.Column>();
        cols.add(TableProto.Column.newBuilder().setString(owner).build());
        cols.add(TableProto.Column.newBuilder().setString(currency).build());
        cols.add(TableProto.Column.newBuilder().setInt64(count).build());
        cols.add(TableProto.Column.newBuilder().setInt64(leftCount).build());
        try {

            TableProto.Row atRow = TableProto.Row.newBuilder()
                    .addAllColumns(cols)
                    .build();
            boolean success = stub.replaceRow(util.getTableAssets(), atRow);
            if (success){
                log.info("Assign to owner:"+owner+"  successfully replaced");
            }

        } catch (Exception e) {
            ret = e.toString();
            return ret;
        }
        log.info("*** Done replaceAsset ***");

        return ret;

    }

}
