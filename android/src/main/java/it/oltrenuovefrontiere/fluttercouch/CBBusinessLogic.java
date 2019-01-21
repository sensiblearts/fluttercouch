package it.oltrenuovefrontiere.fluttercouch;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
// import com.couchbase.lite.Query;
// import com.couchbase.lite.android.AndroidContext;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.JSONMethodCodec;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static android.content.ContentValues.TAG;

/**
 * CBBusinessLogic
 */
// public class CBBusinessLogic implements MethodCallHandler {
public class CBBusinessLogic {

    private static CBManager mCbManager;
    private static CBBusinessLogic mInstance;
   
    private CBBusinessLogic(CBManager manager) {
        mCbManager = manager;
    }

    public static CBBusinessLogic getInstance(CBManager manager) {
        if (mInstance == null) {
            mInstance = new CBBusinessLogic(manager);
        }
        return mInstance;
    }


    // public static void registerWith(Registrar registrar) {
    //     context = registrar.context();
    //     androidContext = new AndroidContext(context);
    //     mCbManager = CBManager.getInstance(androidContext);
    
    //     final FluttercouchPlugin flutterCouchPlugin = new FluttercouchPlugin();
    //     final MethodChannel channel = new MethodChannel(registrar.messenger(), "it.oltrenuovefrontiere.fluttercouch");
    //     channel.setMethodCallHandler(flutterCouchPlugin);

    //     //final MethodChannel jsonChannel = new MethodChannel(registrar.messenger(), "it.oltrenuovefrontiere.fluttercouchJson", JSONMethodCodec.INSTANCE);
    //     //jsonChannel.setMethodCallHandler(new FluttercouchPlugin());

    //     // D.A. removed
    //     // final EventChannel eventChannel = new EventChannel(registrar.messenger(), "it.oltrenuovefrontiere.fluttercouch/replicationEventChannel");
    //     // eventChannel.setStreamHandler(new ReplicationEventListener(flutterCouchPlugin.mCbManager));
    // }


    // private Map<String, Object> getAllEntries() throws CouchbaseLiteException {
    //     Database defaultDb = mDatabase.get(defaultDatabase);
    //     HashMap<String, Object> resultMap = new HashMap<String, Object>();
    //     if (defaultDb != null) {
    //         try {
    //             Document document = defaultDb.getDocument(_id);
    //             if (document != null) {
    //                 resultMap.put("doc", document.getProperties());
    //                 // resultMap.put("doc", document.toMap());
    //                 resultMap.put("id", _id);
    //             } else {
    //                 resultMap.put("doc", null);
    //                 resultMap.put("id", _id);
    //             }
    //         } catch (Exception e) {
    //             e.printStackTrace();
    //         }
    //     }
    //     return resultMap;
    // }

    // TODO: modify getAllEntries to Query Alldocs and return a 
    // List<  Map<String, Object> >
    // TODO: Then, fugure out how to use the flutter ListViewBuilder to
    // send `limit` and `skip` values to getAllEntries, for pagination.
    //
     private String getAllEntries() {
        // Database defaultDb = mDatabase.get(defaultDatabase);
        // use: mDatabase
        String resultMap = "";
        if (mCbManager != null) { // needed?
            // Database db = mCbManager.getDatabase();
            try {
                resultMap = "getAllEntries called successfully";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultMap;
    }

    public void handleCall(MethodCall call, Result result) {
        switch (call.method) {
            case ("getAllEntries"):
                // String _id = call.arguments();
                // try {
                    // result.success(mCbManager.getDocumentWithId(_id));
                    result.success(getAllEntries());
                // } catch (CouchbaseLiteException e) {
                //     e.printStackTrace();
                //     result.error("errAll", "error getting all entries", e.toString());
                // }


                
                break;
            // case ("setReplicatorEndpoint"):
            //     String _endpoint = call.arguments();
            //     try {
            //         String _result = mCbManager.setReplicatorEndpoint(_endpoint);
            //         result.success(_result);
            //     } catch (URISyntaxException e) {
            //         e.printStackTrace();
            //         result.error("errURI", "error setting the replicator endpoint uri to " + _endpoint, e.toString());
            //     }
            //     break;
            // case ("setReplicatorType"):
            //     String _type = call.arguments();
            //     try {
            //         result.success(mCbManager.setReplicatorType(_type));
            //     } catch (CouchbaseLiteException e) {
            //         e.printStackTrace();
            //         result.error("errReplType", "error setting replication type to " + _type, e.toString());
            //     }
            //     break;
            // case ("setReplicatorBasicAuthentication"):
            //     Map<String, String> _auth = call.arguments();
            //     try {
            //         result.success(mCbManager.setReplicatorBasicAuthentication(_auth));
            //     } catch (Exception e) {
            //         e.printStackTrace();
            //         result.error("errAuth", "error setting authentication for replicator", null);
            //     }
            //     break;
            // case ("setReplicatorSessionAuthentication"):
            //     String _sessionID = call.arguments();
            //     try {
            //         result.success(mCbManager.setReplicatorSessionAuthentication(_sessionID));
            //     } catch (Exception e) {
            //         e.printStackTrace();;
            //         result.error("errAuth", "invalid session ID", null);
            //     }
            //     break;
            // case ("setReplicatorContinuous"):
            //     Boolean _continuous = call.arguments();
            //     try {
            //         result.success(mCbManager.setReplicatorContinuous(_continuous));
            //     } catch (Exception e) {
            //         e.printStackTrace();
            //         result.error("errContinuous", "unable to set replication to continuous", null);
            //     }
            //     break;
            // case ("initReplicator"):
            //     mCbManager.initReplicator();
            //     result.success("");
            //     break;
            // case ("startReplicator"):
            //     mCbManager.startReplicator();
            //     result.success("");
            //     break;
            // case ("stopReplicator"):
            //     mCbManager.stopReplicator();
            //     result.success("");
            //     break;
            // case ("executeQuery"):
            //     HashMap<String, String> _queryMap = call.arguments();
            //     Query query = QueryManager.buildFromMap(_queryMap, mCbManager);
            //     try {
            //         result.success(query.explain());
            //     } catch (CouchbaseLiteException e) {
            //         e.printStackTrace();
            //         result.error("errExecutingQuery", "error executing query ", e.toString());
            //     }
            //     break;
            // case ("execute"):
            //     JSONObject queryJson = call.arguments();
            //     Query queryFromJson = new QueryJson(queryJson).toCouchbaseQuery();
            //     break;
            default:
                result.notImplemented();
        }
    }
}
