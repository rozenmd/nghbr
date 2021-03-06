package com.kmji.nghbr.controller;


import com.kmji.nghbr.model.*;
import com.kmji.nghbr.service.AttendeeService;
import com.kmji.nghbr.service.EventService;
import com.kmji.nghbr.service.MessageService;
import com.kmji.nghbr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class EventController extends AbstractController {

    @Autowired
    UserService userService;

    @Autowired
    EventService eventService;

    @Autowired
    AttendeeService attendeeService;

    @Autowired
    MessageService messageService;

    @RequestMapping(value={"/events"}, method = RequestMethod.GET)
    public String events(ModelMap model){
        if (getPrincipal() != "anonymousUser") {
            User user = userService.findBySso(getPrincipal());
            model.addAttribute("user", user);

            Suburb suburb = user.getSuburb();

            List<Event> events = suburb.getEvents();
            model.addAttribute("events", events);

            List<String> eventJSON = new ArrayList<String>();
            for (Event event : events) {
                eventJSON.add(event.getJSONString());
            }
            model.addAttribute("eventsJSON", eventJSON.toString());

            return "event/events";
        } else {
            return "redirect:/login";
        }
    }

    @RequestMapping(value={"/events/{id}"}, method = RequestMethod.GET)
    public String events(@PathVariable String id, ModelMap model){
        User user = userService.findBySso(getPrincipal());
        Event event = eventService.findById(Integer.parseInt(id));

        Attendee attendance = null;

        List<User> usersGoing = new ArrayList<User>();
        List<User> usersNotGoing = new ArrayList<User>();

        List<Attendee> attendees = event.getAttendees();
        for (Attendee attendee : attendees) {
            if (attendee.getUser().equals(user)) {
                attendance = attendee;
            }

            if (attendee.getRsvp()) {
                usersGoing.add(attendee.getUser());
            } else {
                usersNotGoing.add(attendee.getUser());
            }

        }

        model.addAttribute("event", event);
        model.addAttribute("usersGoing", usersGoing);
        model.addAttribute("usersNotGoing", usersNotGoing);
        model.addAttribute("attendance", attendance);

        return "event/show";
    }

    @ResponseBody
    @RequestMapping(value = "/events/new", method = RequestMethod.POST)
    public Event newEvent(@RequestBody Event requestEvent, HttpServletRequest request) {
        User user = userService.findBySso(getPrincipal());

        Event newEvent = new Event();

        newEvent.setTitle(requestEvent.getTitle());
        newEvent.setDescription(requestEvent.getDescription());
        newEvent.setStart(requestEvent.getStart());
        newEvent.setEnd(requestEvent.getEnd());
        newEvent.setOwner(user);
        newEvent.setSuburb(user.getSuburb());

        eventService.saveOrUpdate(newEvent);

        Attendee attendeeHost = new Attendee();
        attendeeHost.setUser(user);
        attendeeHost.setEvent(newEvent);
        attendeeHost.setRsvp(true);

        attendeeService.saveOrUpdate(attendeeHost);

        // Give user a point for creating an event
        user.setPoints(user.getPoints() + 1);
        userService.save(user);

        Message message = new Message();
        message.setUser(user);
        message.setPostCode(user.getSuburb().getPostcode());
        message.setDate(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM YYYY HH:mm");
        String startDate = sdf.format(newEvent.getStart());
        String endDate = sdf.format(newEvent.getEnd());
        message.setText("<i>Hey guys! I'm hosting <a href='/events'>" + newEvent.getTitle() + "</a> which goes from " + startDate + " to " + endDate + ". Hope you see you all there!</i>");
        messageService.save(message);

        return newEvent;
    }

    @ResponseBody
    @RequestMapping(value = "/events/{id}/rsvp", method = RequestMethod.POST)
    public String rsvp(@RequestBody Attendee requestAttendee, HttpServletRequest request, @PathVariable String id) {

        User user = userService.findBySso(getPrincipal());
        Event event = eventService.findById(Integer.parseInt(id));

        List<Attendee> attendees = event.getAttendees();

        Attendee attendee = null;

        for (Attendee a : attendees) {
            if (a.getUser().equals(user)) {
                attendee = a;
            }
        }

        if (attendee == null) {
            attendee = requestAttendee;
            attendee.setUser(user);
            attendee.setEvent(event);
            user.setPoints(user.getPoints()+1); // Give user a point for rsvping an event
            userService.save(user);
        } else {
            boolean rsvp = requestAttendee.getRsvp();
            attendee.setRsvp(rsvp);
        }

        attendeeService.saveOrUpdate(attendee);

        return attendee.toJsonString();
    }



}
