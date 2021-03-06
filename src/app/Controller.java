package app;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
//@CrossOrigin(origins = "http://reactpcr.s3-website-us-east-1.amazonaws.com")
public class Controller {

    private static final String template = "Hello, %s!";

    @RequestMapping("/greeting")
    public String greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return String.format(template, name);
    }

    @RequestMapping("/svd")
    @CrossOrigin(origins = "http://localhost:3000")
//    @CrossOrigin(origins = "http://reactpcr.s3-website-us-east-1.amazonaws.com")
    public List<Recommendation> svd(@RequestParam(value="course1", defaultValue = "") String course1,
                                    @RequestParam(value="course2", defaultValue = "") String course2,
                                    @RequestParam(value="course3", defaultValue = "") String course3,
                                    @RequestParam(value="course4", defaultValue = "") String course4,
                                    @RequestParam(value="course5", defaultValue = "") String course5,
                                    @RequestParam(value="rating1", defaultValue = "") String rating1,
                                    @RequestParam(value="rating2", defaultValue = "") String rating2,
                                    @RequestParam(value="rating3", defaultValue = "") String rating3,
                                    @RequestParam(value="rating4", defaultValue = "") String rating4,
                                    @RequestParam(value="rating5", defaultValue = "") String rating5) {
        Map<String, String> ratings = new HashMap<String, String>();
        ratings.put(course1, rating1);
        ratings.put(course2, rating2);
        ratings.put(course3, rating3);
        ratings.put(course4, rating4);
        ratings.put(course5, rating5);
        return PythonAccess.getRecs(ratings);
    }

    @RequestMapping("/request")
    @CrossOrigin(origins = "http://localhost:3000")
//    @CrossOrigin(origins = "http://reactpcr.s3-website-us-east-1.amazonaws.com")
    public List<Recommendation> request(@RequestParam(value="courses", defaultValue = "") String courses,
                                        @RequestParam(value="courseHistory", defaultValue = "") String courseHistory,
                                        @RequestParam(value="interests", defaultValue = "") String interests,
                                        @RequestParam(value = "diff", defaultValue = "4") String diff,
                                        @RequestParam(value = "courseQual", defaultValue = "4") String courseQual,
                                        @RequestParam(value = "profQual", defaultValue = "4") String profQual) {
        List<Recommendation> recs = new ArrayList<Recommendation>();
//        try ( GraphAccess graph = new GraphAccess( "bolt://ec2-3-85-56-184.compute-1.amazonaws.com:7687", "neo4j", "i-00f51148311d735ce" ) )
        try ( GraphAccess graph = new GraphAccess( "bolt://localhost:7687", "sam", "sam" ) )
        {
            String response = graph.access( courses, interests, diff, courseQual, profQual);
            JSONArray arr = (JSONArray) new JSONParser().parse(response);
            for (Object obj : arr) {
                JSONObject jo = (JSONObject) obj;
                Recommendation rec = new Recommendation(
                        (String) jo.get("name"),
                        (String) jo.get("aliases"),
                        Double.parseDouble((String) jo.get("difficulty")),
                        Double.parseDouble((String) jo.get("courseQuality")),
                        Double.parseDouble((String) jo.get("professorQuality")),
                        (String) jo.get("description")
                );
                recs.add(rec);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        filterCourses(recs, courses, courseHistory);
        return recs;
    }

    private static void filterCourses(List<Recommendation> recs, String coursesLiked, String courseHistory) {
        if (courseHistory.equals("")) {
            return;
        }
        List<Recommendation> toRemove = new ArrayList<Recommendation>();
        String[] ch = courseHistory.split(" ");
        String[] cl = coursesLiked.split(" ");
        for (int i = 0; i < ch.length; i++) {
            for (Recommendation r : recs) {
                if (r.getCode().contains(ch[i])) {
                    toRemove.add(r);
                }
            }
        }
        for (int i = 0; i < cl.length; i++) {
            for (Recommendation r : recs) {
                if (r.getCode().contains(cl[i])) {
                    toRemove.add(r);
                }
            }
        }
        for (Recommendation r : toRemove) {
            recs.remove(r);
        }
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*");
            }
        };
    }

}
