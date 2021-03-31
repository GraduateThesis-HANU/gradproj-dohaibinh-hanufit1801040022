package vn.com.courseman.model.events;

import domainapp.modules.domevents.EventType;

/**
 * @overview 
 *
 * @author Duc Minh Le (ducmle)
 *
 * @version 
 */
public enum CMEventType implements EventType {
  OnCreated,
  OnRemoved,
  OnUpdated
  ;
}
