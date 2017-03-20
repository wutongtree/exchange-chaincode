package krcc;

import org.hyperledger.protos.TableProto;

/**
 * Created by zerppen on 3/19/17.
 */
public class util {

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

    public static int checkRow(TableProto.Row row){

        if(row==null){
            return -1;
        }else if(row.getColumnsCount()==0){
            return 0;
        }else{
            return -2;
        }
    }

    public static String getTableCurrency() {
        return TableCurrency;
    }

    public static String getTableCurrencyReleaseLog() {
        return TableCurrencyReleaseLog;
    }

    public static String getTableCurrencyAssignLog() {
        return TableCurrencyAssignLog;
    }

    public static String getTableAssets() {
        return TableAssets;
    }

    public static String getTableAssetLockLog() {
        return TableAssetLockLog;
    }

    public static String getTableTxLog() {
        return TableTxLog;
    }

    public static String getTableTxLog2() {
        return TableTxLog2;
    }

    public static String getCNY() {
        return CNY;
    }

    public static String getUSD() {
        return USD;
    }

    public static String getCheckErr() {
        return CheckErr;
    }

    public static String getWorldStateErr() {
        return WorldStateErr;
    }
}
