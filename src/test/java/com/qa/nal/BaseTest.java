package com.qa.nal;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    protected Playwright pw;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    protected APIRequestContext apiRequest; // ✅ API context

    @BeforeAll
    void initAll() {
        pw = Playwright.create();
        browser = pw.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(50));
        context = browser.newContext();
        page = context.newPage();

        page.setDefaultTimeout(45000);
        page.onDialog(Dialog::accept);

        // ✅ Create shared API request context
        apiRequest = pw.request().newContext();
    }

    @AfterAll
    void tearDownAll() {
        // ✅ Dispose API context first
        if (apiRequest != null) {
            apiRequest.dispose();
        }

        browser.close();
        pw.close();
    }
}