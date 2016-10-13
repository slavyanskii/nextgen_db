package mail.not.tp.controllers;

import mail.not.tp.db.dao.ForumDAO;
import mail.not.tp.db.dao.UserDAO;
import mail.not.tp.db.dao.impl.ForumDAOImpl;
import mail.not.tp.db.dao.impl.UserDAOImpl;
import mail.not.tp.models.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by viacheslav on 13.10.16.
 */

@RestController
@RequestMapping(value = "/db/api")
public class CommonController {

    @Autowired
    private DataSource dataSource;

    private UserDAO userDAO;
    private ForumDAO forumDAO;
//    private ThreadDAO threadDAO;
//    private PostDAO postDAO;

    @PostConstruct
    void init() {
        userDAO = new UserDAOImpl(dataSource);
        forumDAO = new ForumDAOImpl(dataSource);
//        threadDAO = new ThreadDAOImpl(dataSource);
//        postDAO = new PostDAOImpl(dataSource);
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public Response status() {
        final Map<String, Integer> response = new ConcurrentHashMap<>();
        response.put("user", userDAO.status());
//        response.put("thread", threadDAO.status());
        response.put("forum", forumDAO.status());
//        response.put("post", postDAO.status());
        return new Response(response);
    }

    @RequestMapping(value = "/clear", method = RequestMethod.POST)
    public Response clear() {
        userDAO.clear();
        forumDAO.clear();
//        threadDAO.clear();
//        postDAO.clear();
        return new Response(Response.Codes.OK);
    }
}
