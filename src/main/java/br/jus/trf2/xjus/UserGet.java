package br.jus.trf2.xjus;

import br.jus.trf2.xjus.IXjus.User;
import br.jus.trf2.xjus.IXjus.UserGetRequest;
import br.jus.trf2.xjus.IXjus.UserGetResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class UserGet implements IXjus.IUserGet {

	@Override
	public void run(UserGetRequest req, UserGetResponse resp) throws Exception {
		resp.user = Utils.getUserCorrente();
		if (resp.user == null) {
			resp.user = new User();
			UserService userService = UserServiceFactory.getUserService();
			resp.user.loginUrl = userService.createLoginURL("/");
		}
	}

	public String getContext() {
		return "obter a lista de índices";
	}
}
