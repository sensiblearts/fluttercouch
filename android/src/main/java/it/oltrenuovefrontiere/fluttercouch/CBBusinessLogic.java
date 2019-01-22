package it.oltrenuovefrontiere.fluttercouch;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.View;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.Document;
// import com.couchbase.lite.android.AndroidContext;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.JSONMethodCodec;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static android.content.ContentValues.TAG;

// TODO: copied all imports above; some not needed.

/**
 * CBBusinessLogic
 *
 * Implements delegated functionality from MethodCallHandler in CBManager.java.
*/
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

    // App-specific CB-lite 1.4.4 View for "entries"
    //
    private Boolean initEntriesView() {
        if (mCbManager != null) { // needed?
            Database db = mCbManager.getDatabase();
            View entryView = db.getView("entries");
            entryView.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    if (document.get("doc").equals("ent")) { // is entry
                        List<Object> key = new ArrayList<Object>();
                        key.add(document.get("when"));
                        key.add(document.get("labelId"));
                        emitter.emit(key, null);
                    }
                }
            }, "1");
            return true;
        } else {
            return false;
        }
    }

    // App-specific CB-lite 1.4.4 Query on View for "entries"
    //
    private ArrayList<Map<String, Object>> getAllEntries() {
        ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        if (mCbManager != null) { // needed?
            Database db = mCbManager.getDatabase();
            Query query = db.getView("entries").createQuery();
            query.setDescending(true);
            // query.set...:
            // startKey: the key to start at. The default value, null, means to start from the beginning.
            // endKey: the last key to return. The default value, null, means to continue to the end.
            // descending: If set to true, the keys will be returned in reverse order. (This also reverses the meanings of the startKey and endKey properties, since the query will now start at the highest keys and end at lower ones!)
            // limit: If nonzero, this is the maximum number of rows that will be returned.
            // skip: If nonzero, this many rows will be skipped (starting from the startKey if any.)
            try {
                QueryEnumerator result = query.run();
                for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                    QueryRow row = it.next();
                    // if (row.getConflictingRevisions().size() > 0) {
                    //     Log.w("MYAPP", "Conflict in document: %s", row.getDocumentId());
                    //     beginConflictResolution(row.getDocument());
                    // }
                    results.add(row.getDocument().getProperties());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }
   
    public void handleCall(MethodCall call, Result result) {
        switch (call.method) {
            case ("initView"):
                String view = call.arguments();
                switch(view) {
                    case ("entries"):
                        result.success(initEntriesView());
                        break;
                    default:
                        result.success(false);    
                }
                break;
            case ("getAllEntries"):
                result.success(getAllEntries());
                break;
            default:
                result.notImplemented();
        }
    }
}
