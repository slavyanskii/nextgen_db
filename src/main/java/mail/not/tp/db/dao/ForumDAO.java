package mail.not.tp.db.dao;

import mail.not.tp.models.Response;

/**
 * Created by viacheslav on 13.10.16.
 */
public interface ForumDAO {
    int status();

    void clear();

    Response create(String jsonString);

    Response details(String forum, String[] related);

    Response listPosts(String forum, String since, Integer limit, String order, String[] related);

    Response listThreads(String forum, String since, Integer limit, String order, String[] related);

    Response listUsers(String forum, Integer sinceId, Integer limit, String order);
}
