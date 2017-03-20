package krcc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.java.shim.ChaincodeBase;
import org.hyperledger.java.shim.ChaincodeStub;
import sun.misc.BASE64Decoder;
import org.hyperledger.java.shim.crypto.CryptoPrimitives;


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
        log.info("****** In run, function:"+function+" ******");
        String ret = null;

        run rn = new run();
        if(null==function){
            ret = "Incorrect number of arguments. Expecting 0, No function invoked!";
            return ret;
        }

        switch(function){

            case "Init":
                Init(stub,function,args);
                break;
            case "initAccount":
                rn.initAccount(stub,args);
            case "create":
                rn.create(stub,args);
                break;
            case "releaseCurrency":
                rn.release(stub,args);
                break;
            case "assignCurrency":
                rn.assign(stub,args);
                break;
            case "exchange":
                rn.exchange(stub,args);
                break;
            case "lock":
                rn.lock(stub,args);
                break;
        }
        log.info("****** Done run ******");

        return ret;
    }

    public String Init(ChaincodeStub stub,String function,String[] args){
        log.info("****** In Init ******");
        if((args.length)!=0){
            return "Incorrect number of arguments. Expecting 0";
        }
        init it = new init();
        String retCT = it.createTable(stub);
        if(null!=retCT){
            log.error("Init error1"+retCT);
            return "Init error1"+retCT;
        }else{
            String retIT =it.initTable(stub);
            if(null!=retIT){
                return "Init error2"+retIT;
            }
        }
        log.info("****** Init done ******");
        return null;
    }

    @Override
    public String query(ChaincodeStub stub, String function, String[] args) {

        log.info("****** In query ******");

        String ret = null;
        query qy = new query();
        switch (function){
            case "queryCurrencyByID":
               return qy.queryCurrencyByID(stub, args);
            case "queryAllCurrency":
                return qy.queryAllCurrency(stub, args);
            case "queryTxLogs":
                return qy.queryTxLogs(stub, args);
            case "queryAssetByOwner":
                return qy.queryAssetByOwner(stub, args);
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
        log.info("****** Done query ******");


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
