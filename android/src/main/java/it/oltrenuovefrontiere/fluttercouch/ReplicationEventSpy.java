package it.oltrenuovefrontiere.fluttercouch;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.replicator.ReplicationStateTransition;
// import com.couchbase.lite.replicator.ReplicationTrigger; // NOT PUBLIC -- cannot access
import com.couchbase.lite.replicator.Replication.ChangeListener;

import io.flutter.plugin.common.EventChannel;

import java.util.HashMap;
import java.util.Map;


public class ReplicationEventSpy implements EventChannel.StreamHandler, ChangeListener {

    private CBManager mCBmanager;
    private ListenerToken mListenerToken;
    private EventChannel.EventSink mEventSink;

    ReplicationEventSpy(CBManager _cbManager) {
        this.mCBmanager = _cbManager;
    }

    /*
     * IMPLEMENTATION OF EVENTCHANNEL.STREAMHANDLER
     */

    @Override
    public void onListen(Object o, final EventChannel.EventSink eventSink) {
        mEventSink = eventSink;
        mListenerToken = mCBmanager.getReplicator().addChangeListener(this);
    }

    @Override
    public void onCancel(Object o) {
        mCBmanager.getReplicator().removeChangeListener(mListenerToken);
        mEventSink = null;
    }

    /*
     *  IMPLEMENTATION OF REPLICATORCHANGELISTENER INTERFACE
     */

    @Override
    public void changed(Replication.ChangeEvent change) {
        Throwable error = change.getError();
        if (error != null) {
            mEventSink.error("Exception", "Error during replication", error.getMessage());
        } else {
            Map<String,Object> event = new HashMap<String,Object>();
            // enum ReplicationStatus {
            //         REPLICATION_STOPPED,
            //         REPLICATION_OFFLINE,
            //         REPLICATION_IDLE,
            //         REPLICATION_ACTIVE;
            //         }
            //     }
            switch (change.getStatus()) {
                case REPLICATION_ACTIVE:
                    event.put("status", "BUSY");
                    // mEventSink.success("BUSY");
                    break;
                case REPLICATION_IDLE:
                    event.put("status", "IDLE");
                    // mEventSink.success("IDLE");
                    break;
                case REPLICATION_OFFLINE:
                    event.put("status", "OFFLINE");
                    // mEventSink.success("OFFLINE");
                    break;
                case REPLICATION_STOPPED:
                    event.put("status", "STOPPED");
                    // mEventSink.success("STOPPED");
                    break;
                // case CONNECTING:
                //     mEventSink.success("CONNECTING");
                //     break;
            }
            // enum ReplicationTrigger {
            //     START,
            //     WAITING_FOR_CHANGES,
            //     RESUME,
            //     GO_OFFLINE,
            //     GO_ONLINE,
            //     STOP_GRACEFUL,
            //     STOP_IMMEDIATE;
            //     }
            ReplicationStateTransition transition = change.getTransition();
            if (transition != null){
                Object trigger = (Object)transition.getTrigger();
                if (trigger != null) {
                    event.put("trigger", trigger.toString());
                }
            }
            event.put("change_count", change.getChangeCount());
            event.put("completed_change_count", change.getCompletedChangeCount());
            mEventSink.success(event);
        }
    }
}

// CB 1.4: 
// Observing and monitoring replications
// Since a replication runs asynchronously, if you want to know when it completes or when it gets an error, you’ll need to register as an observer to get notifications from it. The details of this are platform-specific.

// A replication has a number of properties that you can access, especially from a notification callback, to check on its status and progress:

// status: An enumeration that gives the current state of the replication. The values are Stopped, Offline, Idle and Active.

// Stopped: A one-shot replication goes into this state after all documents have been transferred or a fatal error occurs. (Continuous replications never stop.)

// Offline: The remote server is not reachable. Most often this happens because there’s no network connection, but it can also occur if the server’s inside an intranet or home network but the device isn’t. (The replication will monitor the network state and will try to connect when the server becomes reachable.)

// Idle: Indicates that a continuous replication has "caught up" and transferred all documents, but is monitoring the source database for future changes.

// Active: The replication is actively working, either transferring documents or determining what needs to be transferred.

// lastError: The last error encountered by the replicator. (Not all errors are fatal, and a continuous replication will keep running even after a fatal error, by waiting and retrying later.)

// completedChangesCount, changesCount: The number of documents that have been transferred so far, and the estimated total number to transfer in order to catch up. The ratio of these can be used to display a progress meter. Just be aware that changesCount may be zero if the number of documents to transfer isn’t known yet, and in a continuous replication both values will reset to zero when the status goes from Idle back to Active.

// CB 1.4 Replication classes:
//
// @Public
//     public static class ChangeEvent {
//         private final Replication source;
//         private final int changeCount;
//         private final int completedChangeCount;
//         private final Replication.ReplicationStatus status;
//         private final ReplicationStateTransition transition;
//         private final Throwable error;

//         protected ChangeEvent(ReplicationInternal replInternal) {
//             this.source = replInternal.parentReplication;
//             this.changeCount = replInternal.getChangesCount().get();
//             this.completedChangeCount = replInternal.getCompletedChangesCount().get();
//             this.status = Replication.getStatus(replInternal);
//             this.transition = null;
//             this.error = null;
//         }

//         protected ChangeEvent(ReplicationInternal replInternal, ReplicationStateTransition transition) {
//             this.source = replInternal.parentReplication;
//             this.changeCount = replInternal.getChangesCount().get();
//             this.completedChangeCount = replInternal.getCompletedChangesCount().get();
//             this.status = Replication.getStatus(replInternal);
//             this.transition = transition;
//             this.error = null;
//         }

//         protected ChangeEvent(ReplicationInternal replInternal, Throwable error) {
//             this.source = replInternal.parentReplication;
//             this.changeCount = replInternal.getChangesCount().get();
//             this.completedChangeCount = replInternal.getCompletedChangesCount().get();
//             this.status = Replication.getStatus(replInternal);
//             this.transition = null;
//             this.error = error;
//         }

//         public Replication getSource() {
//             return this.source;
//         }

//         public ReplicationStateTransition getTransition() {
//             return this.transition;
//         }

//         public int getChangeCount() {
//             return this.changeCount;
//         }

//         public int getCompletedChangeCount() {
//             return this.completedChangeCount;
//         }

//         public Replication.ReplicationStatus getStatus() {
//             return this.status;
//         }

//         public Throwable getError() {
//             return this.error;
//         }

//         public String toString() {
//             StringBuilder sb = new StringBuilder();
//             sb.append(this.getSource().direction);
//             sb.append(" replication event. Source: ");
//             sb.append(this.getSource());
//             if (this.getTransition() != null) {
//                 sb.append(" Transition: ");
//                 sb.append(this.getTransition().getSource());
//                 sb.append(" -> ");
//                 sb.append(this.getTransition().getDestination());
//             }

//             sb.append(" Total changes: ");
//             sb.append(this.getChangeCount());
//             sb.append(" Completed changes: ");
//             sb.append(this.getCompletedChangeCount());
//             return sb.toString();
//         }
//     }
//
// @Public
//     public interface ChangeListener {
//         void changed(Replication.ChangeEvent var1);
//     }

//     public static enum ReplicationField {
//         FILTER_NAME,
//         FILTER_PARAMS,
//         DOC_IDS,
//         REQUEST_HEADERS,
//         AUTHENTICATOR,
//         CREATE_TARGET,
//         REMOTE_UUID,
//         CHANNELS;

//         private ReplicationField() {
//         }
//     }

//     public static enum ReplicationStatus {
//         REPLICATION_STOPPED,
//         REPLICATION_OFFLINE,
//         REPLICATION_IDLE,
//         REPLICATION_ACTIVE;

//         private ReplicationStatus() {
//         }
//     }

//     public static enum Lifecycle {
//         ONESHOT,
//         CONTINUOUS;

//         private Lifecycle() {
//         }
//     }

//     public static enum Direction {
//         PULL,
//         PUSH;

//         private Direction() {
//         }
//     }
// }