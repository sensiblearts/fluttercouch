package it.oltrenuovefrontiere.fluttercouch;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
// import com.couchbase.lite.Query;
import com.couchbase.lite.android.AndroidContext;

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
 * FluttercouchPlugin
 */
public class FluttercouchPlugin implements MethodCallHandler {

    static CBManager mCbManager;
    static CBBusinessLogic mCbBusinessLogic;
    static AndroidContext androidContext;
    static Context context;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        context = registrar.context();
        androidContext = new AndroidContext(context);
        mCbManager = CBManager.getInstance(androidContext);
        mCbBusinessLogic = CBBusinessLogic.getInstance(mCbManager);
    
        final FluttercouchPlugin flutterCouchPlugin = new FluttercouchPlugin();
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "it.oltrenuovefrontiere.fluttercouch");
        channel.setMethodCallHandler(flutterCouchPlugin);

        //final MethodChannel jsonChannel = new MethodChannel(registrar.messenger(), "it.oltrenuovefrontiere.fluttercouchJson", JSONMethodCodec.INSTANCE);
        //jsonChannel.setMethodCallHandler(new FluttercouchPlugin());

        // D.A. removed for now
        // final EventChannel eventChannel = new EventChannel(registrar.messenger(), "it.oltrenuovefrontiere.fluttercouch/replicationEventChannel");
        // eventChannel.setStreamHandler(new ReplicationEventListener(flutterCouchPlugin.mCbManager));
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        String _id;
        switch (call.method) {
            case ("initDatabaseWithName"):
                String _name = call.arguments();
                try {
                    mCbManager.initDatabaseWithName(_name);
                    result.success(_name);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.error("errInit", "error initializing database", e.toString());
                }
                break;
            case ("saveDocument"):
                Map<String, Object> _document = call.arguments();
                try {
                    Map<String,String> idAndRev = mCbManager.saveDocument(_document);
                    result.success(idAndRev);
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                    result.error("errSave", "error saving the document", e.toString());
                }
                break;
            case ("saveDocumentWithId"):
                if (call.hasArgument("id") && call.hasArgument("map")) {
                    _id = call.argument("id");
                    Map<String, Object> _map = call.argument("map");
                    try {
                        Map<String,String> idAndRev = mCbManager.saveDocumentWithId(_id, _map);
                        result.success(idAndRev);
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                        result.error("errSave", "error saving the document", e.toString());
                    }
                } else {
                    result.error("errArg", "invalid arguments", null);
                }
                break;
            case ("getDocumentWithId"):
                _id = call.arguments();
                try {
                    result.success(mCbManager.getDocumentWithId(_id));
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                    result.error("errGet", "error getting the document with id: " + _id, e.toString());
                }
                break;
            case ("updateDocumentWithId"):
                if (call.hasArgument("id") && call.hasArgument("map")) {
                    try { _id = call.argument("id");
                    Map<String, Object> _map = call.argument("map");
                   
                        Map<String,String> idAndRev = mCbManager.updateDocumentWithId(_id, _map);
                        result.success(idAndRev);
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                        result.error("errUpdate", "error updating the document", e.toString());
                    }
                } else {
                    result.error("errArg", "invalid arguments", null);
                }
                break;
            case ("deleteDocumentWithId"):
                _id = call.arguments(); 
                try {
                    result.success(mCbManager.deleteDocumentWithId(_id));
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                    result.error("errDel", "error deleting the document with id: " + _id, e.toString());
                }
                break;
            case("upsertNamedAttachmentAsFilepath"):
                if (call.hasArgument("id") && call.hasArgument("map")) {
                    _id = call.argument("id");
                    Map<String, Object> _map = call.argument("map");
                    try {
                        Map<String,String> resultMap = mCbManager.upsertNamedAttachmentAsFilepath(_id, _map);
                        result.success(resultMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                        result.error("errSave", "error attaching file", e.toString());
                    }
                } else {
                    result.error("errArg", "invalid arguments", null);
                }
                break;
            case("getNamedAttachment"):
                if (call.hasArgument("id") && call.hasArgument("name")) {
                    _id = call.argument("id");
                    _name = call.argument("name");
                    try {
                        Map<String,Object> resultMap = mCbManager.getNamedAttachment(_id, _name);
                        result.success(resultMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                        result.error("errSave", "error getting attachment", e.toString());
                    }
                } else {
                    result.error("errArg", "invalid arguments", null);
                }
                break;
            case ("createReplicatorWithName"):
                String _repname = call.arguments();
                try {
                    mCbManager.createReplicatorWithName(_repname);
                    result.success("");
                } catch (Exception e) {
                    e.printStackTrace();
                    result.error("errReplcreate", "error creating configuring replicator", null);
                }
                break;
//         if (_config.containsKey("username") 
//                 && _config.containsKey("password")
//                 && _config.containsKey("url")
//                 && _config.containsKey("synctype")
//                 && _config.containsKey("continuous")) {
            case ("configureReplicator"):
                Map<String, String> _config = call.arguments();
                try {
                    result.success(mCbManager.configureReplicator(_config));
                } catch (Exception e) {
                    e.printStackTrace();
                    result.error("errReplconfig", "error configuring replicator", null);
                }
                break;
            case ("startReplicator"):
                try {
                    mCbManager.startReplicator();
                    result.success("");
                } catch (Exception e) {
                    e.printStackTrace();
                    result.error("errReplstart", "error starting replicator", null);
                }
                break;
            case ("stopReplicator"):
                try {
                    mCbManager.stopReplicator();
                    result.success("");
                } catch (Exception e) {
                    e.printStackTrace();
                    result.error("errReplstop", "error stopping replicator", null);
                }
                break;
            default:
                mCbBusinessLogic.handleCall(call, result);
            }
    }
}
