package krcc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.java.shim.ChaincodeStub;
import org.hyperledger.protos.TableProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zerppen on 3/19/17.
 */
public class init {
    private static Log log = LogFactory.getLog(init.class);



    public String createTable(ChaincodeStub stub){

        String ret = null;
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
            stub.createTable(util.getTableCurrency(),cols);
        }catch (Exception e){
            retStr = e.toString();
            log.error("createTable error1:"+retStr);
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
            stub.createTable(util.getTableCurrencyReleaseLog(),cols);
        }catch (Exception e){
            retStr = e.toString();
            log.error("createTable error2:"+retStr);
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
            stub.createTable(util.getTableCurrencyAssignLog(),cols);
        }catch (Exception e){
            retStr = e.toString();
            log.error("createTable error3:"+retStr);
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
            stub.createTable(util.getTableAssets(),cols);
        }catch (Exception e){
            retStr = e.toString();
            log.error("createTable error4:"+retStr);
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
            stub.createTable(util.getTableAssetLockLog(),cols);
        }catch (Exception e){
            retStr = e.toString();
            log.error("createTable error5:"+retStr);
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
            stub.createTable(util.getTableTxLog(),cols);
        }catch (Exception e){
            retStr = e.toString();
            log.error("createTable error6:"+retStr);
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
            stub.createTable(util.getTableTxLog2(),cols);
        }catch (Exception e){
            retStr = e.toString();
            log.error("createTable error7:"+retStr);
            return retStr;
        }

        return ret;

    }

    public String initTable(ChaincodeStub stub){

        String ret = null;
        TableProto.Column col1 = TableProto.Column.newBuilder().setString(util.getCNY()).build();
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

            boolean success = stub.insertRow(util.getTableCurrency(), rows);

            if (success){
                log.info("Row CNY successfully inserted");
            }
        } catch (Exception e) {
            ret = e.toString();
            log.error("createTable error1:"+ret);
            return ret;
        }
        col1 = TableProto.Column.newBuilder().setString(util.getUSD()).build();
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

            boolean success = stub.insertRow(util.getTableCurrency(), rows);

            if (success){
                log.info("Row USD successfully inserted");
            }
        } catch (Exception e) {
            ret = e.toString();
            log.error("createTable error2:"+ret);
            return ret;
        }


        return ret;
    }
}
