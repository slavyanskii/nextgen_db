package mail.not.tp.db.dao;

import mail.not.tp.models.Response;

/**
 * Created by viacheslav on 14.10.16.
 */
public interface PostDAO {
    int status();

    void clear();


    Response create(String jsonString);

    Response details(int postId, String[] related);

    Response listForumPosts(String forum, String since, Integer limit, String order);

    Response listThreadPosts(int threadId, String since, Integer limit, String order);

    Response remove(String jsonString);

    Response restore(String jsonString);

    Response update(String jsonString);

    Response vote(String jsonString);
}
