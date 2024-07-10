package camp.nextstep.controller;

import camp.nextstep.db.InMemoryUserRepository;
import camp.nextstep.domain.http.HttpCookie;
import camp.nextstep.domain.http.HttpRequest;
import camp.nextstep.domain.http.HttpResponse;
import camp.nextstep.domain.session.Session;
import camp.nextstep.domain.session.SessionManager;
import camp.nextstep.model.User;

import java.util.Map;

public class LoginController extends AbstractController {

    private static final String INDEX_PAGE_PATH = "/index.html";
    private static final String UNAUTHORIZED_PAGE_PATH = "/401.html";

    private static final String LOGIN_ACCOUNT_KEY = "account";
    private static final String LOGIN_PASSWORD_KEY = "password";

    @Override
    protected HttpResponse doPost(HttpRequest httpRequest) {
        Map<String, String> requestBody = httpRequest.getHttpRequestBody();
        String account = requestBody.get(LOGIN_ACCOUNT_KEY);
        String password = requestBody.get(LOGIN_PASSWORD_KEY);
        User user = InMemoryUserRepository.findByAccount(account)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 account입니다."));
        if (user.checkPassword(password)) {
            return handleLoginUser(httpRequest, user);
        }
        return HttpResponse.found(httpRequest.getHttpProtocol(), UNAUTHORIZED_PAGE_PATH);
    }

    private HttpResponse handleLoginUser(HttpRequest httpRequest, User user) {
        Session session = Session.createNewSession();
        session.setAttribute("user", user);
        SessionManager.add(session);
        return HttpResponse.found(httpRequest.getHttpProtocol(), INDEX_PAGE_PATH)
                .addCookie(HttpCookie.sessionCookie(session));
    }

    @Override
    protected HttpResponse doGet(HttpRequest httpRequest) {
        if (httpRequest.containsSessionId() && isLoginSession(httpRequest)) {
            return HttpResponse.found(httpRequest.getHttpProtocol(), INDEX_PAGE_PATH);
        }
        return handleStaticPath(httpRequest);
    }

    private boolean isLoginSession(HttpRequest httpRequest) {
        String sessionId = httpRequest.getSessionId();
        return SessionManager.findSession(sessionId)
                .isPresent();
    }
}
