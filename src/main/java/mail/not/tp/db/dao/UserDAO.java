package mail.not.tp.db.dao;

import mail.not.tp.models.Response;

/**
 * Created by viacheslav on 13.10.16.
 */
public interface UserDAO {
    int status();

    void clear();

    Response create(String jsonString);

    Response details(String email);

    Response follow(String jsonString);

    Response unfollow(String jsonString);

    Response listFollowers(String email, Integer limit, String order, Integer sinceId);

    Response listFollowing(String email, Integer limit, String order, Integer sinceId);

    Response listPosts(String email, Integer limit, String order, String since);

    Response updateProfile(String jsonString);
}
