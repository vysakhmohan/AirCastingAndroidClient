package pl.llp.aircasting.tracking;

import pl.llp.aircasting.activity.events.SessionStoppedEvent;
import pl.llp.aircasting.helper.LocationHelper;
import pl.llp.aircasting.helper.MetadataHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.storage.DatabaseTaskQueue;
import pl.llp.aircasting.storage.repository.SessionRepository;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * Created by ags on 03/16/13 at 23:36
 */
public class ContinuousTracker
{
  @Inject EventBus eventBus;
  @Inject MetadataHelper metadataHelper;
  @Inject LocationHelper locationHelper;
  @Inject SettingsHelper settingsHelper;
  @Inject DatabaseTaskQueue taskQueue;
  @Inject SessionRepository sessions;

  private Session session;

  private SessionTracker sessionTracker;
  private ActualNoteTracker noteTracker;

  @Inject
  public void init()
  {
    eventBus.register(this);
    sessionTracker = new InactiveSessionTracker(taskQueue);
    noteTracker = new ActualNoteTracker(eventBus, taskQueue);
  }

  public void addNote(Note note)
  {
    sessionTracker.addNote(note);
  }

  public void startTracking(Session incomingSession)
  {
    this.session = incomingSession;
    sessionTracker = new ActualSessionTracker(eventBus, session, taskQueue, settingsHelper, metadataHelper, sessions);
    sessionTracker.save(session);
  }

  public void stopTracking(Session session)
  {
    sessionTracker.finishTracking();
    sessionTracker = new InactiveSessionTracker(taskQueue);
    eventBus.post(new SessionStoppedEvent(session));
  }

  public void setTitle(long sessionId, String title)
  {
    sessionTracker.setTitle(sessionId, title);
  }

  public void setTags(long sessionId, String tags)
  {
    sessionTracker.setTags(sessionId, tags);
  }

  public void setDescription(long sessionId, String description)
  {
    sessionTracker.setDescription(sessionId, description);
  }

  public void setContribute(long sessionId, boolean shouldContribute)
  {
    sessionTracker.setContribute(sessionId, shouldContribute);
  }

  public void addStream(final MeasurementStream stream)
  {
    sessionTracker.addStream(stream);
  }

  public void addMeasurement(MeasurementStream stream, Measurement measurement)
  {
    sessionTracker.addMeasurement(stream, measurement);
  }

  public void complete(long sessionId)
  {
    sessions.complete(sessionId);
  }

  public void discard(long sessionId)
  {
    stopTracking(session);
    sessionTracker.delete(sessionId);
  }

  public void deleteNote(Session session, Note note)
  {
    noteTracker.deleteNote(session, note);
  }
}

