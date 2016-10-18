package mail.not.tp.db.dao;

import mail.not.tp.models.Response;

/**
 * Created by viacheslav on 14.10.16.
 */
public interface ThreadDAO {
    int status();

    void clear();

    Response create(String jsonString);

    Response details(int threadId, String[] related);

    Response listPosts(int threadId, String since, Integer limit, String sort, String order);

    Response listUserThreads(String user, String since, Integer limit, String order);

    Response listForumThreads(String forum, String since, Integer limit, String order);

    Response remove(String jsonString);

    Response restore(String jsonString);

    Response update(String jsonString);

    Response vote(String jsonString);

    Response subscribe(String jsonString);

    Response unsubscribe(String jsonString);

    Response open(String jsonString);

    Response close(String jsonString);
}
