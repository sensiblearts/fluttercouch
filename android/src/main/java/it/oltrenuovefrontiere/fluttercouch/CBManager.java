package it.oltrenuovefrontiere.fluttercouch;

//import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.Database;
//import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
//import com.couchbase.lite.Endpoint;
//import com.couchbase.lite.LogDomain;
//import com.couchbase.lite.LogLevel;
import com.couchbase.lite.util.Log;
//import com.couchbase.lite.MutableDocument;

//import com.couchbase.lite.Replicator;
//import com.couchbase.lite.ReplicatorConfiguration;
//
//import com.couchbase.lite.SessionAuthenticator;
//import com.couchbase.lite.URLEndpoint;
//import com.couchbase.litecore.C4Replicator;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

// SEE: https://docs.couchbase.com/couchbase-lite/1.4/java.html#manager

// TODO: Bring back replicator functionality for cb 1.4.4

public class CBManager {
    private static AndroidContext _context = null;
    private Manager _manager = null;
    
    private static CBManager mInstance = null;
    private HashMap<String, Database> mDatabase = new HashMap<>();
//    private ReplicatorConfiguration mReplConfig;
//    private Replicator mReplicator;
    private String defaultDatabase = "defaultdatabase";

    private CBManager(AndroidContext context) {
             try {
                _manager = new Manager(context, Manager.DEFAULT_OPTIONS);
             } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public static CBManager getInstance(AndroidContext context) {
        if (mInstance == null) {
            _context = context;
            mInstance = new CBManager(context);
        }
        return mInstance;
    }

    public Database getDatabase() {
        return mDatabase.get(defaultDatabase);
    }

    public Database getDatabase(String name) {
        if (mDatabase.containsKey(name)) {
            return mDatabase.get(name);
        }
        return null;
    }

     public Map<String, String> saveDocument(Map<String, Object> _map) throws CouchbaseLiteException {
        HashMap<String, String> resultMap = new HashMap<String, String>();
        Document doc = mDatabase.get(defaultDatabase).createDocument();
        doc.putProperties(_map);
        resultMap.put("_id", doc.getId());
        resultMap.put("_rev", (String)doc.getProperties().get("_rev"));
        return resultMap;
    }

    public Map<String, String> saveDocumentWithId(String _id, Map<String, Object> _map) throws CouchbaseLiteException {
        HashMap<String, String> resultMap = new HashMap<String, String>();
        Document doc = mDatabase.get(defaultDatabase).getDocument(_id);
        doc.putProperties(_map);
        resultMap.put("_id", doc.getId());
        resultMap.put("_rev", (String)doc.getProperties().get("_rev"));
        return resultMap;
    }

    public Map<String, String> updateDocumentWithId(String _id, Map<String, Object> _map) throws CouchbaseLiteException {
        HashMap<String, String> resultMap = new HashMap<String, String>();
        Document doc = mDatabase.get(defaultDatabase).getDocument(_id);
        // TODO: doc valid? exist?
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.putAll(_map);
        try {
            doc.putProperties(properties);
            resultMap.put("_id", doc.getId());
            resultMap.put("_rev", (String)doc.getProperties().get("_rev"));
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            resultMap.put("_id", null);
            resultMap.put("_rev", null);
        }
        return resultMap;
    }

    public Map<String, Object> getDocumentWithId(String _id) throws CouchbaseLiteException {
        Database defaultDb = mDatabase.get(defaultDatabase);
        HashMap<String, Object> resultMap = new HashMap<String, Object>();
        if (defaultDb != null) {
            try {
                Document document = defaultDb.getDocument(_id);
                if (document != null) {
                    resultMap.put("doc", document.getProperties());
                    // resultMap.put("doc", document.toMap());
                    resultMap.put("id", _id);
                } else {
                    resultMap.put("doc", null);
                    resultMap.put("id", _id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultMap;
    }

    public boolean deleteDocumentWithId(String _id) throws CouchbaseLiteException {
        Document doc = mDatabase.get(defaultDatabase).getDocument(_id);
        return doc.delete();
    }
    
    public void initDatabaseWithName(String _name) throws CouchbaseLiteException {
    //    DatabaseConfiguration config = new DatabaseConfiguration(FluttercouchPlugin.context);
    //    if (!mDatabase.containsKey(_name)) {
    //        defaultDatabase = _name;
    //        // Database.setLogLevel(LogDomain.REPLICATOR, LogLevel.VERBOSE);
    //        mDatabase.put(_name, new Database(_name, config));
    //    }
       if (!mDatabase.containsKey(_name)) {
           defaultDatabase = _name;
           Database db = _manager.getDatabase(_name);
           mDatabase.put(_name, db);
       }
   }

//    public String setReplicatorEndpoint(String _endpoint) throws URISyntaxException {
//        Endpoint targetEndpoint = new URLEndpoint(new URI(_endpoint));
//        mReplConfig = new ReplicatorConfiguration(mDatabase.get(defaultDatabase), targetEndpoint);
//        return mReplConfig.getTarget().toString();
//    }

//    public String setReplicatorType(String _type) throws CouchbaseLiteException {
//        ReplicatorConfiguration.ReplicatorType settedType = ReplicatorConfiguration.ReplicatorType.PULL;
//        if (_type.equals("PUSH")) {
//            settedType = ReplicatorConfiguration.ReplicatorType.PUSH;
//        } else if (_type.equals("PULL")) {
//            settedType = ReplicatorConfiguration.ReplicatorType.PULL;
//        } else if (_type.equals("PUSH_AND_PULL")) {
//            settedType = ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL;
//        }
//        mReplConfig.setReplicatorType(settedType);
//        return settedType.toString();
//    }
//
//    public String setReplicatorBasicAuthentication(Map<String, String> _auth) throws Exception {
//        if (_auth.containsKey("username") && _auth.containsKey("password")) {
//            mReplConfig.setAuthenticator(new BasicAuthenticator(_auth.get("username"), _auth.get("password")));
//        } else {
//            throw new Exception();
//        }
//        return mReplConfig.getAuthenticator().toString();
//    }
//
//    public String setReplicatorSessionAuthentication(String sessionID) throws Exception {
//        if (sessionID != null) {
//            mReplConfig.setAuthenticator(new SessionAuthenticator(sessionID));
//        } else {
//            throw new Exception();
//        }
//        return mReplConfig.getAuthenticator().toString();
//    }
//
//    public boolean setReplicatorContinuous(boolean _continuous) {
//        mReplConfig.setContinuous(_continuous);
//        return mReplConfig.isContinuous();
//    }
//
//    public void initReplicator() {
//        mReplicator = new Replicator(mReplConfig);
//    }
//
//    public void startReplicator() {
//        mReplicator.start();
//    }
//
//    public void stopReplicator() {
//        mReplicator.stop();
//        mReplicator = null;
//    }
//
//    public Replicator getReplicator() {
//        return mReplicator;
//    }
}
