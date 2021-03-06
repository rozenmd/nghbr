package com.kmji.nghbr.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmji.nghbr.model.Suburb;
import com.kmji.nghbr.service.SuburbService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.web.servlet.ModelAndView;

import org.springframework.beans.factory.annotation.Autowired;

import com.kmji.nghbr.model.User;
import com.kmji.nghbr.service.UserService;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UserController extends AbstractController {

    @Autowired
    UserService userService;
    @Autowired
    SuburbService suburbService;

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public ModelAndView adminPage() {
        ModelAndView model = new ModelAndView("user/admin");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            User user = userService.findBySso(getPrincipal());
            if (user.getSuburb() == null) {
                return new ModelAndView("redirect:user/initialise");
            }
            if(!(user.getEmail().equals("maximusblu@gmail.com"))){
                return new ModelAndView("redirect:profile");
            }
            model.addObject("user", user);
            try {



            } catch (Exception e) {
                System.err.println("Got an exception! ");
                System.err.println(e.getMessage());
            }
            //model.addAttribute("user", getPrincipal());
        }
        //model.addAttribute("users", userService.findAllUsers());
        return model;
    }

    @RequestMapping(value = "/db", method = RequestMethod.GET)
    public String dbaPage(ModelMap model) {
        model.addAttribute("user", getPrincipal());
        return "user/dba";
    }

    @RequestMapping(value = "/totalPoints", method = RequestMethod.GET)
    public String totalPoints(ModelMap model) {
        model.addAttribute("user", getPrincipal());
        List<User> users = userService.findAllUsers();
        List<Suburb> suburbs = suburbService.findAllSuburbs();
        String result = "";
        int countusers = 0;
        int countadds = 0;
        for(Suburb suburb : suburbs){
            int tp = 0;
            for(User user : users){
                    if((user.getSuburb().getSuburbName().equals(suburb.getSuburbName()))
                            && (user.getSuburb().getPostcode()==suburb.getPostcode())){
                        tp += user.getPoints();
                        countadds++;
                    }
                countusers++;
            }
            suburb.setTotalPoints(tp);
            suburbService.save(suburb);
        }
        System.out.println("numUsers counted: " + countusers + " users added: " + countadds);
        return "redirect: user/profile";

    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage() {

        if (getPrincipal() != "anonymousUser") {
            return "redirect:/";
        } else {
            return "user/login";
        }
    }

    @RequestMapping(value="/logout", method = RequestMethod.GET)
    public String logoutPage (HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/";
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public ModelAndView profilePage(){
        ModelAndView model = new ModelAndView("user/profile");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            User user = userService.findBySso(getPrincipal());
            if(user.getSuburb() == null){
                return new ModelAndView("redirect:user/initialise");
            }
            model.addObject("user", user);
            try{
                if(user.getSuburb().getSuburbName().length() > 0 && user.getSuburb().getPostcode() > 0){
                    Suburb suburb = suburbService.findByPostcodeSuburb(
                            user.getSuburb().getPostcode(),
                            user.getSuburb().getSuburbName());
                    model.addObject("lat", suburb.getLat());
                    model.addObject("lon", suburb.getLon());
                    model.addObject("points", suburb.getTotalPoints());
                    model.addObject("suburb",suburb.toString());
                }else if (user.getSuburb().getPostcode() > 0){
                	Suburb suburb = suburbService.findByPostcode(user.getSuburb().getPostcode()).get(0);
                    model.addObject("lat", suburb.getLat());
                    model.addObject("lon", suburb.getLon());
                    model.addObject("points", suburb.getTotalPoints());
                    model.addObject("suburb",suburb.toString());
                } else if (user.getSuburb().getPostcode() < 0){
                	Suburb suburb = suburbService.findBySuburb(user.getSuburb().getSuburbName());
                    model.addObject("lat", suburb.getLat());
                    model.addObject("lon", suburb.getLon());
                    model.addObject("points", suburb.getTotalPoints());
                    model.addObject("suburb",suburb.toString());
                }

            }catch (Exception e) {
                System.err.println("Got an exception! ");
                System.err.println(e.getMessage());
            }
        }
        return model;
    }

    @RequestMapping(value = "/user/update", method = RequestMethod.GET)
    public ModelAndView updateProfile() {
        ModelAndView model = new ModelAndView("user/update");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            User user = userService.findBySso(getPrincipal());
            model.addObject("user", user);
        }
        return model;
    }
    @RequestMapping(value = "/user/update", method = RequestMethod.POST)
    public ModelAndView updateProfilePOST(HttpServletRequest request){
        //int postcode=-1;
        ModelAndView model = new ModelAndView("/user/update");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        //try parse a postcode to int
        try{
            int postcode = Integer.parseInt(request.getParameter("postcode"));
            Suburb suburb = null;
            if(request.getParameter("suburb").length() > 0 && postcode > 0){
                 suburb = suburbService.findByPostcodeSuburb(
                         postcode,
                        request.getParameter("suburb")
                );
            }else if (postcode > 0){
                 suburb = suburbService.findByPostcode(postcode).get(0);
            } else if (!(postcode > 0) ){
                 suburb = suburbService.findBySuburb(request.getParameter("suburb"));
            }
  
            String email = request.getParameter("email");
            User user = userService.findBySso(getPrincipal());

            System.out.println(user.toString());

            user.setFirstName(firstName);
            user.setLastName(lastName);
            if(suburb != null){
                user.setSuburb(suburb);
                System.out.println(suburb.getSuburbName() + " " + suburb.getPostcode());
            }

            //suburbService.save()
            user.setEmail(email);
            userService.save(user);
            return new ModelAndView("redirect:../profile");
        }catch (Exception e) {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }

        return model;

    }

    @RequestMapping(value = "/user/initialise", method = RequestMethod.GET)
    public ModelAndView initialise() {
        ModelAndView model = new ModelAndView("user/initialise");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            User user = userService.findBySso(getPrincipal());
            model.addObject("user", user);
        }
        return model;
    }

    @RequestMapping(value = "/user/initialise", method = RequestMethod.POST)
    public ModelAndView initialisePOST(HttpServletRequest request){

        ModelAndView model = new ModelAndView("user/initialise");

        try{
            int postcode = Integer.parseInt(request.getParameter("postcode"));
            Suburb suburb = null;
            if(request.getParameter("suburb").length() > 0 && postcode > 0){
                suburb = suburbService.findByPostcodeSuburb(
                        postcode,
                        request.getParameter("suburb")
                );
            }else if (postcode > 0){
                suburb = suburbService.findByPostcode(postcode).get(0);
            } else if (!(postcode > 0) ){
                suburb = suburbService.findBySuburb(request.getParameter("suburb"));
            }

            User user = userService.findBySso(getPrincipal());
            System.out.println(user.toString());

            if(suburb != null){
                user.setSuburb(suburb);
                user.setPoints(user.getPoints()+1);
                userService.save(user);
                model = new ModelAndView("redirect:/profile");
            } else {
                model.addObject("alertMessage", "Sorry, we could not find your suburb. Please make you sure you have entered the correct suburb and/or postcode!");
            }

        }catch (Exception e) {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
            model.addObject("alertMessage", "Sorry, we could not find your suburb. Please make you sure you have entered the correct suburb and/or postcode!");
        }

        return model;

    }

    @RequestMapping(value = "/user/scoreboard", method = RequestMethod.GET)
    public ModelAndView scoreboard() {
        ModelAndView model = new ModelAndView("/user/scoreboard");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            User user = userService.findBySso(getPrincipal());
            model.addObject("user", user);
            try {
                if (user.getSuburb().getSuburbName().length() > 0 && user.getSuburb().getPostcode() > 0) {
                    Suburb suburb = suburbService.findByPostcodeSuburb(
                            user.getSuburb().getPostcode(),
                            user.getSuburb().getSuburbName()
                    );
                    model.addObject("lat", suburb.getLat());
                    model.addObject("lon", suburb.getLon());
                    model.addObject("points", suburb.getTotalPoints());
                    model.addObject("suburb",suburb.toString());
                } else if (user.getSuburb().getPostcode() > 0) {
                    //Will just pick first suburb in list...
                    Suburb suburb = suburbService.findByPostcode(user.getSuburb().getPostcode()).get(0);
                    model.addObject("lat", suburb.getLat());
                    model.addObject("lon", suburb.getLon());
                    model.addObject("points", suburb.getTotalPoints());
                    model.addObject("suburb",suburb.toString());
                } else if (user.getSuburb().getPostcode() < 0) {

                    Suburb suburb = suburbService.findBySuburb(user.getSuburb().getSuburbName());
                    model.addObject("lat", suburb.getLat());
                    model.addObject("lon", suburb.getLon());
                    model.addObject("points", suburb.getTotalPoints());
                    model.addObject("suburb",suburb.toString());
                }
            } catch (Exception e) {
                System.err.println("Got an exception! ");
                System.err.println(e.getMessage());
            }

            List<Suburb> topSuburbs = suburbService.findTopFifteenSuburbs();
            //ObjectMapper mapper = new ObjectMapper();
            String jsonSuburb = "[";
            for(Suburb suburb: topSuburbs){
                jsonSuburb += suburb.getJSONString();
                jsonSuburb += ",";
            }
            jsonSuburb += "]";
//            try {
//               jsonSuburb = mapper.writeValueAsString(topSuburbs);
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//            }
            System.out.println(jsonSuburb);
            model.addObject("jsonSuburb", jsonSuburb);

            String geoJsonSuburb = "{\"type\": \"FeatureCollection\",\"features\": [";
            for(Suburb suburb: topSuburbs){
                geoJsonSuburb += suburb.getGEOJsonString();
                geoJsonSuburb += ",";
            }
            geoJsonSuburb = geoJsonSuburb.substring(0,geoJsonSuburb.length()-1);
            geoJsonSuburb +="]}";

            System.out.println(geoJsonSuburb);
            model.addObject("geoJsonSuburb",geoJsonSuburb);
        }
        return model;

    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String registerPage(ModelMap model) {
        // create a test user
        try {
            User user = new User();
            user.setFirstName("test");
            user.setLastName("user");
            user.setSsoId("test");
            user.setEmail("test@example.com");
            user.setPassword("Pass.123");
            userService.save(user);
            model.addAttribute("user", user);
        } catch(Exception e) {
            model.addAttribute("user", userService.findBySso("test"));
        }

        model.addAttribute("user", getPrincipal());
        return "user/register";
    }


}
