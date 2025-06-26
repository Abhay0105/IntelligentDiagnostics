package com.qa.nal;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import com.qa.nal.utils.ExcelReader;
import io.github.cdimascio.dotenv.Dotenv;
import io.qase.commons.annotation.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import org.junit.jupiter.api.*;
import org.slf4j.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("My Application - Core User Flows")
public class DiagnosticTests extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(DiagnosticTests.class);

    Dotenv dotenv = Dotenv.load();
    private final String username = dotenv.get("APP_USERNAME");
    private final String password = dotenv.get("PASSWORD");
    private final String loginUrl = dotenv.get("DEVDEMO_URL");

    @Test
    @Order(1)
    @QaseId(1)
    @QaseTitle("Navigate to Login Page")
    public void navigateToLoginPage() {
        try {
            page.navigate(loginUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD));
            page.waitForURL(url -> url.contains("login"), new Page.WaitForURLOptions().setTimeout(45000));
            Assertions.assertTrue(page.url().contains("login"), "Not redirected on login page");
            log.info("Navigating to login page: {}", loginUrl);
        } catch (Exception e) {
            log.info("Navigating to login page: {}", page.url());
            Assertions.fail("Login page not found: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @QaseId(2)
    @QaseTitle("Perform Login")
    public void performLogin() {
        try {
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username")).click();
            log.info("Username field clicked");

            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username")).fill(username);
            log.info("Username field filled");

            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password")).click();
            log.info("Password field clicked");

            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password")).fill(password);
            log.info("Password field filled");

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login").setExact(true)).click();
            log.info("Login button clicked");

            page.waitForURL(url -> url.contains("/app/new-home"));

            Assertions.assertTrue(page.url().contains("new-home"), "Login did not navigate to home");
            log.info("Login successful, navigated to home page");
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            Assertions.fail("Login failed: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @QaseId(3)
    @QaseTitle("Handle Initial Pop-Up")
    public void handleInitialPopup() {
        try {
            page.locator(".modal-content");
            log.info("Modal Pop-Up found");

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cancel")).click();
            log.info("Cancel button clicked");
        } catch (Exception e) {
            Assertions.fail("Modal not found or not visible: " + e.getMessage());
            log.error("No modal found");
        }
    }

    // Intelligent Diagnostics
    @Test
    @Order(4)
    @QaseId(4)
    @QaseTitle("Navigate to Intelligent Diagnostics")
    public void navigateToIntelligentDiagnostics() {
        try {
            page.locator("body > app > default-layout > div > aside > div.sidebar.collapsed").hover();
            log.info("Sidebar hovered");

            page.waitForTimeout(2000);

            page.locator("a").filter(new Locator.FilterOptions().setHasText("Intelligent Diagnostics")).click();
            log.info("Intelligent Diagnostics clicked");
        } catch (Exception e) {
            log.error("Navigation to Intelligent Diagnostics failed: {}", e.getMessage());
            Assertions.fail("Navigation to Intelligent Diagnostics failed: " + e.getMessage());
        }
    }

    // Service Request
    @Test
    @Order(5)
    @QaseId(5)
    @QaseTitle("Navigate to Service Request")
    public void navigateToServiceRequest() {
        try {
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(" Service Request")).click();
            log.info("Service Request clicked");

            page.waitForURL(url -> url.contains("caseobject"));
            Assertions.assertTrue(page.url().contains("caseobject"), "Service Request page did not load as expected");

            log.info("Service Request page loaded successfully");
        } catch (Exception e) {
            log.error("Navigation to Service Request failed: {}", e.getMessage());
            Assertions.fail("Navigation to Service Request failed: " + e.getMessage());
        }
    }

    @Test
    @Order(6)
    @QaseId(6)
    @QaseTitle("Create Service Request")
    public void createNewServiceRequest() {
        try {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("  Create New")).click();
            log.info("Create New button clicked");

            page.waitForSelector("#modalCenter > div > div", new Page.WaitForSelectorOptions().setTimeout(45000));
            log.info("Create New modal opened");

            page
                    .locator("div")
                    .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Manufacturer \\*$")))
                    .nth(1)
                    .click();

            log.info("Picklist clicked");

            page.waitForTimeout(2000);

            List<Locator> manufacturersList = page.locator("//ng-dropdown-panel//div[@role='option']").all();

            Random random = new Random();
            int randomIndex = random.nextInt(manufacturersList.size());
            log.info("Random index selected: {}", randomIndex);
            String selectedManufacturer = manufacturersList.get(randomIndex).textContent().trim();
            manufacturersList.get(randomIndex).click();

            log.info("Manufacturer option clicked: {}", selectedManufacturer);

            page.getByRole(AriaRole.TEXTBOX).click();

            // getting descriptions from excel sheet
            List<String> descriptionsList = ExcelReader.readDescriptionsFromExcel(
                    "src/test/resources/srDescriptions.xlsx",
                    "devdemo");

            randomIndex = random.nextInt(descriptionsList.size());

            // fill random description
            page.getByRole(AriaRole.TEXTBOX).fill(descriptionsList.get(randomIndex));

            page.getByRole(AriaRole.TEXTBOX).press("Tab");

            // Click Start Diagnosis button
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Start Diagnosis")).click();
            log.info("Start Diagnosis button clicked");

            // Wait for navigation
            page.waitForURL(url -> url.contains("n7-predictions"), new Page.WaitForURLOptions().setTimeout(15000));
            log.info("Navigated to predictions page");

            Assertions.assertTrue(page.url().contains("n7-predictions"), "Predictions page did not load as expected");
            log.info("Predictions page loaded successfully");

            // Wait until loading screen disappears
            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

            page.waitForLoadState(LoadState.NETWORKIDLE);
        } catch (Exception e) {
            log.error("Error occurred: {}", e.getMessage());
            Assertions.fail("Test Failed: " + e.getMessage());
        }
    }

    // private Boolean checkObs() {
    // Boolean obsFound = false;

    // try {
    // page.waitForSelector(
    // ".loading-screen-wrapper",
    // new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
    // );

    // page.waitForLoadState(LoadState.NETWORKIDLE);

    // page.waitForSelector(
    // ".loading-screen-wrapper",
    // new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
    // );

    // page.waitForTimeout(3000);

    // page.waitForSelector(".observation-card", new
    // Page.WaitForSelectorOptions().setTimeout(15000));
    // log.info("Observation card found");

    // page.waitForTimeout(5000);

    // page.waitForLoadState(LoadState.NETWORKIDLE);

    // List<Locator> problemList = page.locator(".observation-card").all();
    // log.info("Problem List size: {}", problemList.size());

    // String problemText = problemList.get(0).textContent().trim();

    // if (!problemText.contains("Are you seeing something else?")) {
    // obsFound = true;
    // log.info("Observation card found: {}", problemText);
    // } else {
    // obsFound = false;
    // log.info("Observation card not found");
    // }

    // return obsFound;
    // } catch (Exception e) {
    // log.error("Observation card not found: {}", e.getMessage());
    // Assertions.fail("Observation card not found: " + e.getMessage());
    // return obsFound;
    // }
    // }

    private Boolean checkObs() {
        boolean obsFound = false;
        boolean defaultCardFound = false;
        int maxAttempts = 6;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("Attempt {} to check for observation cards", attempt);

                // Wait for loading screen to disappear
                page.waitForSelector(
                        ".loading-screen-wrapper",
                        new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(10000));

                page.waitForLoadState(LoadState.NETWORKIDLE);

                page.waitForTimeout(1500);

                Locator obsLocator = page.locator(".observation-card");
                int count = obsLocator.count();

                log.info("Observation card count: {}", count);

                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        String text = obsLocator.nth(i).textContent().trim();
                        log.info("Observation[{}]: {}", i, text);

                        if (!text.contains("Are you seeing something else?")) {
                            log.info("Valid observation found: {}", text);
                            obsFound = true;
                            break;
                        } else {
                            defaultCardFound = true;
                        }
                    }
                } else {
                    log.info("No observation cards found.");
                }

                if (obsFound)
                    break;
            } catch (Exception e) {
                log.warn("Exception on attempt {}: {}", attempt, e.getMessage());
            }
        }

        if (!obsFound && !defaultCardFound) {
            log.error("No observation card (even default) found after {} attempts", maxAttempts);
            Assertions.fail("No observation card found after multiple retries.");
        }

        return obsFound; // true = valid found, false = only default card
    }

    private void newObs() {
        try {
            page
                    .locator("mat-card")
                    .filter(new Locator.FilterOptions().setHasText("Are you seeing something else?"))
                    .click();
            log.info("Something Else button clicked");

            page.locator("mat-card-content").getByRole(AriaRole.COMBOBOX).click();
            page.locator("mat-card-content").getByRole(AriaRole.COMBOBOX).fill("New Observation");
            log.info("New Observation field filled");

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create New")).click();
            log.info("Create New button clicked");
        } catch (Exception e) {
            log.error("New Observation not found: {}", e.getMessage());
            Assertions.fail("New Observation not found: " + e.getMessage());
        }
    }

    private Boolean checkInf() {
        Boolean infFound = false;
        try {
            boolean newSolutionCreated = false;

            page.waitForTimeout(1500);

            page.waitForLoadState(LoadState.NETWORKIDLE);

            List<Locator> solutionList = new ArrayList<Locator>();
            try {
                page.waitForSelector(
                        ".resolution",
                        new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
                log.info("Inference found");

                solutionList = page.locator(".resolution").all();
            } catch (Exception e) {
                log.error("No Inference found: {}", e.getMessage());
            }

            if (!solutionList.isEmpty()) {
                log.info("Solutions size: {}", solutionList.size());
                infFound = true;
                log.info("Solutions found: {}", solutionList.size());
            } else if (!newSolutionCreated) {
                newSolutionCreated = true;
                log.info("No solution found!");
                infFound = false;
            }

            return infFound;
        } catch (Exception e) {
            log.error("Existing Inference not found: {}", e.getMessage());
            Assertions.fail("Existing Inference not found: " + e.getMessage());
            return infFound;
        }
    }

    private void existingInfCheckbox() {
        try {
            page.locator("mat-card-header.list-item").locator("i.fa-square").first().click();
            log.info("Checkbox clicked");
        } catch (Exception e) {
            log.error("Checkbox not Clicked: {}", e.getMessage());
            Assertions.fail("Checkbox not Clicked: " + e.getMessage());
        }
    }

    private void newInf() {
        try {
            log.info("No solution found!");
            page
                    .locator("div")
                    .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Search\\/Create$")))
                    .first()
                    .click();
            log.info("Input field clicked");

            page.locator("mat-drawer-content").getByRole(AriaRole.COMBOBOX).fill("New Solution");
            log.info("Solution field filled");

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create New")).click();
            log.info("Solution created");

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
        } catch (Exception e) {
            log.error("New Inference not found: {}", e.getMessage());
            Assertions.fail("New Inference not found: " + e.getMessage());
        }
    }

    private void saveAndContinue() {
        try {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save & Continue")).click();
            log.info("Save and Continue button clicked");

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
        } catch (Exception e) {
            log.error("Save and Continue button not found: {}", e.getMessage());
            Assertions.fail("Save and Continue button not found: " + e.getMessage());
        }
    }

    private void saveAndClose() {
        try {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save & Close")).click();
            log.info("Save and Close button clicked");

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Copy")).click();

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
        } catch (Exception e) {
            log.error("Save and Close button not found: {}", e.getMessage());
            Assertions.fail("Save and Close button not found: " + e.getMessage());
        }
    }

    private void createNewObs() {
        try {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("  Create New")).click();
            log.info("Create New button clicked");

            page.waitForSelector("#modalCenter > div > div", new Page.WaitForSelectorOptions().setTimeout(45000));
            log.info("Create New modal opened");

            page
                    .locator("div")
                    .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Manufacturer \\*$")))
                    .nth(1)
                    .click();
            log.info("Manufacturer field clicked");

            page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("ALINITY_S")).click();
            log.info("ALINITY-S option clicked");

            page
                    .getByLabel("1Please fill out the")
                    .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Next"))
                    .click();
            log.info("Next button clicked");

            page.waitForTimeout(2000);

            page.locator(".new-obsrv-select > .ng-select-container").click();
            log.info("Text box clicked");

            page
                    .locator("ng-select")
                    .filter(new Locator.FilterOptions().setHasText("Search/CreateType to search"))
                    .getByRole(AriaRole.COMBOBOX)
                    .fill("test observation");
            log.info("Observation field filled");

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create New").setExact(true)).click();
            log.info("Create New button clicked");

            page
                    .locator("div")
                    .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Search\\/Create$")))
                    .nth(1)
                    .click();
            log.info("Inference field clicked");

            page
                    .locator("ng-select")
                    .filter(new Locator.FilterOptions().setHasText("Search/CreateType to"))
                    .getByRole(AriaRole.COMBOBOX)
                    .fill("test inference");
            log.info("Inference field filled");

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create New").setExact(true)).click();
            log.info("Create New button clicked");

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save")).click();
            log.info("Save button clicked");

            page
                    .getByLabel("2Observation Details")
                    .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Next"))
                    .click();
            log.info("Next button clicked");

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Observation")).click();
            log.info("Save Observation button clicked");
        } catch (Exception e) {
            log.error("Test Failed: {}", e.getMessage());
            Assertions.fail("Test Failed: " + e.getMessage());
        }
    }

    private void existingObsTypeAhead() {
        try {
            page
                    .locator("mat-card")
                    .filter(new Locator.FilterOptions().setHasText("Are you seeing something else?"))
                    .click();
            log.info("Something Else button clicked");

            page.locator("mat-card-content").getByRole(AriaRole.COMBOBOX).click();

            char[] alphabets = "abcdefghilmnoprst".toCharArray();
            log.info("Alphabets used for Type Ahead: {}", alphabets);

            Random random = new Random();
            int index = random.nextInt(alphabets.length);

            char randomChar = alphabets[index];
            log.info("Random character generated: {}", randomChar);

            page.locator("mat-card-content").getByRole(AriaRole.COMBOBOX).fill("" + randomChar);
            log.info("New Observation field filled");

            page.waitForTimeout(1500);

            page.waitForSelector(
                    "div[role='listbox'][aria-label='Options List'].ng-dropdown-panel-items",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
            log.info("Type Ahead listbox found");

            page.waitForSelector(
                    "div[role='option'].ng-option",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
            log.info("Obs Options are visible");

            List<Locator> exObsTAList = page.locator("div[role='option'].ng-option").all();
            log.info("Obs List:- " + exObsTAList);

            page.waitForTimeout(1500);

            log.info("Observation list size:- " + exObsTAList.size());

            if (!exObsTAList.isEmpty() || exObsTAList.size() != 0) {
                int randomIndex = random.nextInt(exObsTAList.size());
                log.info("Random index selected: {}", randomIndex);
                String selectedObservation = exObsTAList.get(randomIndex).textContent().trim();
                exObsTAList.get(randomIndex).click();
                log.info("Existing Observation Type Ahead option clicked: {}", selectedObservation);

                page.waitForTimeout(1500);
            }
        } catch (Exception e) {
            log.error("Existing Observation not found: {}", e.getMessage());
            Assertions.fail("Existing Observation not found: " + e.getMessage());
        }
    }

    private void existingInfTypeAhead() {
        try {
            page
                    .locator("div")
                    .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Search\\/Create$")))
                    .first()
                    .click();
            log.info("Input field clicked");

            char[] alphabets = "cdehilmnost".toCharArray();
            log.info("Alphabets used for Type Ahead: {}", alphabets);

            Random random = new Random();
            int index = random.nextInt(alphabets.length);

            char randomChar = alphabets[index];
            log.info("Random character generated: {}", randomChar);

            page.locator("mat-drawer-content").getByRole(AriaRole.COMBOBOX).fill("" + randomChar);
            log.info("Solution field filled");

            page.waitForTimeout(1500);

            page.waitForSelector(
                    "div[role='listbox'][aria-label='Options List'].ng-dropdown-panel-items",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
            log.info("Type Ahead listbox found");

            page.waitForSelector(
                    "div[role='option'].ng-option",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
            log.info("Inf Options are visible");

            List<Locator> exInfTAList = page.locator("div[role='option'].ng-option").all();
            log.info("Inf List:- " + exInfTAList);

            page.waitForTimeout(1500);

            log.info("Inference list size:- " + exInfTAList.size());

            if (!exInfTAList.isEmpty() || exInfTAList.size() != 0) {
                int randomIndex = random.nextInt(exInfTAList.size());
                log.info("Random index selected: {}", randomIndex);
                String selectedInference = exInfTAList.get(randomIndex).textContent().trim();
                exInfTAList.get(randomIndex).click();
                log.info("Existing Inference Type Ahead option clicked: {}", selectedInference);

            }

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
        } catch (Exception e) {
            log.error("New Inference not found: {}", e.getMessage());
            Assertions.fail("New Inference not found: " + e.getMessage());
        }
    }

    @Test
    @Order(7)
    @QaseId(7)
    @QaseTitle("Existing Observation and Existing Inference")
    public void exObsExInf() {
        try {
            if (checkObs()) {
                log.info("Observation card found");
                if (checkInf()) {
                    log.info("Existing Inference found");

                    existingInfCheckbox();

                    saveAndContinue();
                } else {
                    log.info("Existing Inference not found");

                    page
                            .getByRole(AriaRole.HEADING,
                                    new Page.GetByRoleOptions().setName("Do you want to resolve with"))
                            .click();

                    log.info("New Solution clicked");

                    existingInfTypeAhead();

                    saveAndContinue();
                }
            } else {
                log.info("Observation card not found");

                existingObsTypeAhead();

                if (checkInf()) {
                    log.info("Existing Inference found");

                    existingInfCheckbox();

                    saveAndContinue();
                } else {
                    log.info("Existing Inference not found");

                    page
                            .getByRole(AriaRole.HEADING,
                                    new Page.GetByRoleOptions().setName("Do you want to resolve with"))
                            .click();

                    log.info("New Solution clicked");

                    existingInfTypeAhead();

                    saveAndContinue();
                }
            }
        } catch (Exception e) {
            log.error("Test Failed: {}", e.getMessage());
            Assertions.fail("Test Failed: " + e.getMessage());
        }
    }

    @Test
    @Order(8)
    @QaseId(8)
    @QaseTitle("Delete Investigation")
    public void deleteInvestigation() {
        try {
            page.locator("mat-card-title i").click();
            log.info("Delete button clicked");

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Yes")).click();
            log.info("Yes button clicked");

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

            // Wait for the loading screen to disappear
            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

            // Wait for the page to load completely
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // Wait for the loading screen to disappear
            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
            log.info("Investigation deleted successfully");

            // Wait for the page to load completely
            page.waitForLoadState(LoadState.NETWORKIDLE);

            page.locator("app-new-diagnostic form div.wrapper.observations div.col-12").locator("button").click();
            log.info("Expand button clicked");
        } catch (Exception e) {
            log.error("Test Failed: {}", e.getMessage());
            Assertions.fail("Test Failed: " + e.getMessage());
        }
    }

    @Test
    @Order(9)
    @QaseId(9)
    @QaseTitle("New Observation and New Inference")
    public void newObsNewInf() {
        try {
            if (checkObs()) {
                log.info("Observation card found");
                newObs();

                if (checkInf()) {
                    log.info("Existing Inference found");

                    newInf();

                    saveAndContinue();
                } else {
                    log.info("Existing Inference not found");

                    newInf();

                    saveAndContinue();
                }
            } else {
                newObs();

                log.info("Observation card not found");
                if (checkInf()) {
                    log.info("Existing Inference found");

                    existingInfCheckbox();

                    saveAndContinue();
                } else {
                    log.info("Existing Inference not found");

                    newInf();

                    saveAndContinue();
                }
            }

            // deleteInvestigation();
            deleteInvestigation();
        } catch (Exception e) {
            log.error("Test Failed: {}", e.getMessage());
            Assertions.fail("Test Failed: " + e.getMessage());
        }
    }

    // Type Ahead
    @Test
    @Order(10)
    @QaseId(10)
    @QaseTitle("Existing Observation and existing Inference(Type Ahead)")
    public void exObsExInfTA() {
        try {
            if (checkObs()) {
                log.info("Observation card found");

                existingObsTypeAhead();

                if (checkInf()) {
                    log.info("Existing Inference found");

                    page
                            .getByRole(AriaRole.HEADING,
                                    new Page.GetByRoleOptions().setName("Do you want to resolve with"))
                            .click();
                    log.info("New Solution clicked");

                    existingInfTypeAhead();

                    saveAndContinue();
                } else {
                    log.info("Existing Inference not found");

                    page
                            .getByRole(AriaRole.HEADING,
                                    new Page.GetByRoleOptions().setName("Do you want to resolve with"))
                            .click();
                    log.info("New Solution clicked");

                    existingInfTypeAhead();

                    saveAndContinue();
                }
            } else {
                log.info("Observation card not found");

                existingObsTypeAhead();

                if (checkInf()) {
                    log.info("Existing Inference found");

                    existingInfTypeAhead();

                    saveAndContinue();
                } else {
                    log.info("Existing Inference not found");

                    existingInfTypeAhead();

                    saveAndContinue();
                }
            }

            // deleteInvestigation();
            deleteInvestigation();
        } catch (Exception e) {
            log.error("Test Failed: {}", e.getMessage());
            Assertions.fail("Test Failed: " + e.getMessage());
        }
    }

    @Test
    @Order(11)
    @QaseId(11)
    @QaseTitle("Existing Observation and New Inference")
    public void exObsNewInfExInf() {
        try {
            if (checkObs()) {
                log.info("Observation card found");
                if (checkInf()) {
                    log.info("Existing Inference found");

                    existingInfCheckbox();

                    page
                            .getByRole(AriaRole.HEADING,
                                    new Page.GetByRoleOptions().setName("Do you want to resolve with"))
                            .click();
                    log.info("New Solution clicked");

                    newInf();

                    saveAndClose();
                } else {
                    log.info("Existing Inference not found");

                    existingInfTypeAhead();

                    page
                            .getByRole(AriaRole.HEADING,
                                    new Page.GetByRoleOptions().setName("Do you want to resolve with"))
                            .click();
                    log.info("New Solution clicked");

                    newInf();

                    saveAndClose();
                }
            } else {
                existingObsTypeAhead();

                log.info("Observation card not found");
                if (checkInf()) {
                    log.info("Existing Inference found");

                    existingInfCheckbox();

                    page
                            .getByRole(AriaRole.HEADING,
                                    new Page.GetByRoleOptions().setName("Do you want to resolve with"))
                            .click();
                    log.info("New Solution clicked");

                    newInf();

                    saveAndClose();
                } else {
                    log.info("Existing Inference not found");

                    existingInfTypeAhead();

                    page
                            .getByRole(AriaRole.HEADING,
                                    new Page.GetByRoleOptions().setName("Do you want to resolve with"))
                            .click();
                    log.info("New Solution clicked");

                    newInf();

                    saveAndClose();
                }
            }
        } catch (Exception e) {
            log.error("Test Failed: {}", e.getMessage());
            Assertions.fail("Test Failed: " + e.getMessage());
        }
    }

    // Observation Management
    @Test
    @Order(12)
    @QaseId(12)
    @QaseTitle("Navigate to Observation Management")
    public void navigateToObsManagement() {
        try {
            handleInitialPopup();

            page.locator("body > app > default-layout > div > aside > div.sidebar.collapsed").hover();
            log.info("Sidebar hovered");

            page.waitForTimeout(2000);

            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(" Observation Management")).click();
            log.info("Observation Management clicked");

            page.waitForURL(url -> url.contains("observation-mgmt"), new Page.WaitForURLOptions().setTimeout(45000));
            Assertions.assertTrue(
                    page.url().contains("observation-mgmt"),
                    "Not redirected on Observation Management page");
            log.info("Navigated to Observation Management page");
        } catch (Exception e) {
            log.error("Navigation to Observation Management failed: {}", e.getMessage());
            Assertions.fail("Navigation to Observation Management failed: " + e.getMessage());
        }
    }

    @Test
    @Order(13)
    @QaseId(13)
    @QaseTitle("Create new Observation in Observation Management")
    public void createNewObsOM() {
        createNewObs();
    }

    @Test
    @Order(14)
    @QaseId(14)
    @QaseTitle("Edit Observation")
    public void editObservation() {
        try {
            page.locator(".action-btn").first().click();
            log.info("Action Button Clicked");

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

            page.waitForLoadState(LoadState.NETWORKIDLE);
        } catch (Exception e) {
            log.error("Action Button not found: {}", e.getMessage());
            Assertions.fail("Action Button not found: " + e.getMessage());
        }
    }

    @Test
    @Order(15)
    @QaseId(15)
    @QaseTitle("Child Observation in Observation Management")
    public void childObservation() {
        try {
            // create New Inference
            page.getByText("Child Observation ", new Page.GetByTextOptions().setExact(true)).click();

            page.locator("input[type=\"text\"]").click();

            page.locator("input[type=\"text\"]").fill("Test");
            log.info("Inference field filled");

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

            page.waitForSelector("div[role='option'].ng-option");

            List<Locator> obsList = page.locator("div[role='option'].ng-option").all();

            if (obsList.size() > 0) {
                log.info("Observations found: {}", obsList.size());
                Random random = new Random();
                int randomIndex = random.nextInt(obsList.size());
                obsList.get(randomIndex).click();
                log.info("Observation selected at index: {}", randomIndex);

                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Yes")).click();
                log.info("Yes button clicked");
            } else {
                log.info("No observations found to select from.");
            }
        } catch (Exception e) {
            log.error("Create New Child Inference failed: {}", e.getMessage());
            Assertions.fail("Create New Child Inference failed: " + e.getMessage());
        }
    }

    // Inference Management
    @Test
    @Order(16)
    @QaseId(16)
    @QaseTitle("Navigate To Inference Management")
    public void navigateToInferenceManagement() {
        try {
            page.locator("body > app > default-layout > div > aside > div.sidebar.collapsed").hover();
            log.info("Sidebar hovered");

            page.waitForTimeout(2000);

            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Inference Management")).click();
            log.info("Inference Management clicked");

            page.waitForURL(url -> url.contains("inference-mgmt"), new Page.WaitForURLOptions().setTimeout(45000));
            Assertions.assertTrue(page.url().contains("inference-mgmt"), "Not redirected on Inference Management page");
            log.info("Navigated to Inference Management page");
        } catch (Exception e) {
            log.error("Navigation to Inference Management failed: {}", e.getMessage());
            Assertions.fail("Navigation to Inference Management failed: " + e.getMessage());
        }
    }

    @Test
    @Order(17)
    @QaseId(17)
    @QaseTitle("Edit Inference")
    public void editInf() {
        try {
            page.locator(".action-btn").first().click();
            log.info("Action Button Clicked");

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
        } catch (Exception e) {
            log.error("Edit Inference Button Click failed: {}", e.getMessage());
            Assertions.fail("Edit Inference Button Click failed: " + e.getMessage());
        }
    }

    @Test
    @Order(18)
    @QaseId(18)
    @QaseTitle("Merge Similar Inferences")
    public void mergeSimilarInf() {
        try {
            page.getByText("Similar Inferences", new Page.GetByTextOptions().setExact(true)).click();
            log.info("Similar Inferences clicked");

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

            List<Locator> checkboxes;
            try {
                page.waitForSelector("input[type='checkbox'].form-check-input");
                checkboxes = page.locator("input[type='checkbox'].form-check-input").all();
            } catch (Exception e) {
                checkboxes = new ArrayList<>();
                log.error("No Similar Inferences found!");
            }

            log.info("Similar Inferences List size: {}", checkboxes.size());

            if (!checkboxes.isEmpty()) {
                int randomIndex = new Random().nextInt(checkboxes.size());
                checkboxes.get(randomIndex).check();
                log.info("Checked random Similar Inference checkbox at index: {}", randomIndex);
            } else {
                log.info("No Similar Inferences found to check");
            }
        } catch (Exception e) {
            log.error("Merge Similar Inferences failed: {}", e.getMessage());
            Assertions.fail("Merge Similar Inferences failed: " + e.getMessage());
        }
    }

    @Test
    @Order(19)
    @QaseId(19)
    @QaseTitle("Create New Child Inference")
    public void createNewChildInf() {
        try {
            // create New Inference
            page.getByText("Child Inferences", new Page.GetByTextOptions().setExact(true)).click();

            page.locator("input[type=\"text\"]").click();

            page.locator("input[type=\"text\"]").fill("Child Inf");
            log.info("Inference field filled");

            page.getByText("Create New").click();
            log.info("Create New button clicked");

            page.locator("app-rich-text-editor").getByText("Save").click();
            log.info("Save button clicked");

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Yes")).click();
            log.info("Yes button clicked");

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
        } catch (Exception e) {
            log.error("Create New Child Inference failed: {}", e.getMessage());
            Assertions.fail("Create New Child Inference failed: " + e.getMessage());
        }
    }

    @Test
    @Order(20)
    @QaseId(20)
    @QaseTitle("Create New Observation in Inference Management")
    public void createObs() {
        try {
            // create New Inference
            page.getByText("Observations ", new Page.GetByTextOptions().setExact(true)).click();

            page.locator("input[type=\"text\"]").click();

            page.locator("input[type=\"text\"]").fill("New Observation");
            log.info("Observation field filled");

            page.getByText("Create New").click();
            log.info("Create New button clicked");

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
        } catch (Exception e) {
            log.error("Create New Child Inference failed: {}", e.getMessage());
            Assertions.fail("Create New Child Inference failed: " + e.getMessage());
        }
    }

    // Multimedia Management
    @Test
    @Order(21)
    @QaseId(21)
    @QaseTitle("Navigate To Multimedia Management")
    public void navigateToMultimediaManagement() {
        try {
            page.locator("body > app > default-layout > div > aside > div.sidebar.collapsed").hover();
            log.info("Sidebar hovered");

            page.waitForTimeout(2000);

            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(" Multimedia Management")).click();
            log.info("Multimedia Management clicked");

            page.waitForURL(url -> url.contains("multimedia"), new Page.WaitForURLOptions().setTimeout(45000));
            Assertions.assertTrue(page.url().contains("multimedia"), "Not redirected on Multimedia Management page");
            log.info("Navigated to Multimedia Management page");
        } catch (Exception e) {
            log.error("Navigation to Multimedia Management failed: {}", e.getMessage());
            Assertions.fail("Navigation to Multimedia Management failed: " + e.getMessage());
        }
    }

    @Test
    @Order(22)
    @QaseId(22)
    @QaseTitle("Create New Multimedia")
    public void createNewMultimedia() {
        try {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("  Create New")).click();
            log.info("Create New button clicked");
            // upload file
            Locator fileInput = page.locator("input[type='file']");

            fileInput.waitFor(new Locator.WaitForOptions().setTimeout(2000));

            fileInput.hover();

            log.info("File input hovered");
            page.evaluate("selector => document.querySelector(selector).style.display = 'block'", "input[type='file']");
            Path filePath = Paths.get("C:\\Users\\abhay\\Downloads\\demo_Image.jpg");

            fileInput.setInputFiles(filePath.toAbsolutePath());

            page.waitForTimeout(1000);
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Please Enter Asset Tags")).click();

            page.waitForTimeout(1000);
            page
                    .getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Please Enter Asset Tags"))
                    .fill("Demo Image 1");

            page.waitForTimeout(1000);
            page
                    .getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Please Enter Asset Tags"))
                    .press("Tab");

            // page.getByRole(AriaRole.BUTTON, new
            // Page.GetByRoleOptions().setName("Upload")).click();
            page.locator("button:has-text('Upload')").click();

            log.info("File uploaded successfully!");
        } catch (Exception e) {
            log.error("Create New Multimedia failed: {}", e.getMessage());
            Assertions.fail("Create New Multimedia failed: " + e.getMessage());
        }
    }

    @Test
    @Order(23)
    @QaseId(23)
    @QaseTitle("Edit Multimedia")
    public void editMultimedia() {
        try {
            page.locator(".action-btn").first().click();
            log.info("Edit Button Clicked");

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

            page.waitForLoadState(LoadState.NETWORKIDLE);

            // edit file
            Locator fileInput = page.locator("input[type='file']");

            fileInput.waitFor(new Locator.WaitForOptions().setTimeout(2000));

            fileInput.hover();
            log.info("File input hovered");
            page.evaluate("selector => document.querySelector(selector).style.display = 'block'", "input[type='file']");

            Path filePath = Paths.get("C:\\Users\\abhay\\Downloads\\Demo_Image2.jpg");

            fileInput.setInputFiles(filePath.toAbsolutePath());
            page.waitForTimeout(1000);

            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Please Enter Asset Tags")).click();
            page.waitForTimeout(1000);

            page
                    .getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Please Enter Asset Tags"))
                    .fill("Demo Image 2");
            page.waitForTimeout(1000);

            page
                    .getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Please Enter Asset Tags"))
                    .press("Tab");

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Update File")).click();
            log.info("File uploaded successfully!");

            page.waitForTimeout(5000);

            // delete file
            page.locator(".delete-btn").first().click();
            log.info("Delete button clicked");
            page.waitForTimeout(2000);
        } catch (Exception e) {
            log.error("Edit Multimedia Button Click failed: {}", e.getMessage());
            Assertions.fail("Edit Multimedia Button Click failed: " + e.getMessage());
        }
    }

    // Inbox
    @Test
    @Order(24)
    @QaseId(24)
    @QaseTitle("Navigate to Inbox")
    public void navigateToInbox() {
        try {
            page.locator("body > app > default-layout > div > aside > div.sidebar.collapsed").hover();
            log.info("Sidebar hovered");

            page.waitForTimeout(2000);

            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(" Inbox")).click();
            log.info("Inbox clicked");

            page.waitForURL(url -> url.contains("inbox"), new Page.WaitForURLOptions().setTimeout(45000));
            Assertions.assertTrue(page.url().contains("inbox"), "Not redirected on Inbox page");
            log.info("Navigated to Inbox page");
        } catch (Exception e) {
            log.error("Navigation to Inbox failed: {}", e.getMessage());
            Assertions.fail("Navigation to Inbox failed: " + e.getMessage());
        }
    }

    @Test
    @Order(25)
    @QaseId(25)
    @QaseTitle("Merge Observations")
    public void mergeObs() {
        // Merge Observations
        page
                .locator(
                        ".ag-row-odd > div:nth-child(9) > .ag-cell-wrapper > .ag-cell-value > app-icon-renderer > i:nth-child(4)")
                .first()
                .click();

        try {
            page.waitForSelector("li.result-text");
            List<Locator> allObservationOptions = page.locator("li.result-text").all();
            log.info("All Observation Options size: {}", allObservationOptions.size());

            Random random = new Random();
            int randomIndex = random.nextInt(allObservationOptions.size());
            Locator radio = allObservationOptions.get(randomIndex).locator("input[type='radio']");

            radio.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

            radio.check();
            log.info("New Observation radio button checked");

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Merge Into")).click();
            log.info("Merge Into button clicked");
        } catch (Exception e) {
            log.info("New Observation radio button not found!");

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cancel")).click();
            log.info("Cancel button clicked");
        }
    }

    @Test
    @Order(26)
    @QaseId(26)
    @QaseTitle("Create New Observations")
    public void createNewObsInbox() {
        // Create New Observation
        createNewObs();

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("last")).click();

        log.info("Last Clicked");
        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("first")).click();

        log.info("First Clicked");

        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
    }

    @Test
    @Order(27)
    @QaseId(27)
    @QaseTitle("Approve Observations")
    public void approveObs() {
        // Approve Inferences
        page.locator(".fa-check").first().click();

        log.info("Approve Clicked");

        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
    }

    private void selectInference() {
        // Select Inference
        page.waitForSelector(
                "input[type='checkbox'][aria-label*='toggle row selection']",
                new Page.WaitForSelectorOptions().setTimeout(15000));

        log.info("Inference Found");

        List<Locator> infList = page.locator("input[type='checkbox'][aria-label*='toggle row selection']").all();

        log.info("Inference List size: {}", infList.size());

        if (!infList.isEmpty()) {
            infList.get(0).click();
            log.info("Inference Selected.");
        } else {
            log.info("No Inference Found.");
        }
    }

    @Test
    @Order(28)
    @QaseId(28)
    @QaseTitle("Apporve Inferences")
    public void approveInf() {
        // Bulb Icon Click
        page.locator("i.fa-lightbulb-o").nth(4).click();

        log.info("Bulb icon Clicked");

        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

        page.waitForLoadState(LoadState.NETWORKIDLE);

        // Select Inference
        selectInference();

        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

        // Approve Inferences
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("  Approve Inferences")).click();
        log.info("Approve Inferences Clicked");

        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Yes")).click();
        log.info("Yes Clicked");

        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

        page.waitForLoadState(LoadState.NETWORKIDLE);

        log.info("Inference Approved Successfully");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close")).click();
        log.info("Close Clicked");

        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

        page.waitForTimeout(2000);
    }

    @Test
    @Order(29)
    @QaseId(29)
    @QaseTitle("Delete Inferences")
    public void deleteInf() {
        page.waitForTimeout(2000);
        // Bulb Icon Click
        page.locator("i.fa-lightbulb-o").nth(4).click();

        log.info("Bulb icon Clicked");

        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

        page.waitForLoadState(LoadState.NETWORKIDLE);

        page.waitForTimeout(2000);

        // Select Inference
        selectInference();
        // page.locator("input[type='checkbox'][aria-label*='toggle row
        // selection']").click();

        page.waitForTimeout(2000);
        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

        // Delete Inferences
        Locator deleteInferenceButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("  Delete Inferences"));

        deleteInferenceButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("  Delete Inferences")).click();
        log.info("Delete Inferences Clicked");

        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Yes")).click();
        log.info("Yes Clicked");

        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close")).click();
        log.info("Close Clicked");

        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
    }

    @Test
    @Order(30)
    @QaseId(30)
    @QaseTitle("Edit Observations")
    public void editObs() {
        page.locator(".action-btn").first().click();

        log.info("Action Button Clicked");

        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
    }

    @Test
    @Order(31)
    @QaseId(31)
    @QaseTitle("Create New Inference")
    public void createNewInf() {
        // create New Inference
        page.getByText("Inference", new Page.GetByTextOptions().setExact(true)).click();

        page.locator("input[type=\"text\"]").click();

        page.locator("input[type=\"text\"]").fill("test inf");
        log.info("Inference field filled");

        page.getByText("Create New").click();
        log.info("Create New button clicked");

        page.locator("app-rich-text-editor").getByText("Save").click();
        log.info("Save button clicked");

        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
    }

    @Test
    @Order(32)
    @QaseId(32)
    @QaseTitle("Reject Inference")
    public void rejectInf() {
        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

        page.waitForSelector("li a div[title='Reject Inference']");

        page.locator("li a div[title='Reject Inference']").first().click();

        log.info("Reject clicked");

        page.getByPlaceholder("Comments").click();
        log.info("Comments field clicked");

        page.getByPlaceholder("Comments").fill("Invailid");
        log.info("Comments field filled");

        page.getByText("Submit").click();
        log.info("Submit button clicked");
        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
    }

    @Test
    @Order(33)
    @QaseId(33)
    @QaseTitle("Delete Observations")
    public void deleteObs() {
        // Delete Observation
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete")).click();
        log.info("Delete Clicked");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Yes")).click();
        log.info("Yes Clicked");

        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
    }

    @Test
    @Order(34)
    @QaseId(34)
    @QaseTitle("Navigate To Self Service Diagnostics")
    public void navigateToSelfServiceDiagnostics() {
        try {
            page.locator("body > app > default-layout > div > aside > div.sidebar.collapsed").hover();
            log.info("Sidebar hovered");

            page.waitForTimeout(2000);

            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(" Self Service Diagnostics")).click();
            log.info("Self Service Diagnostics clicked");

            page.waitForURL(url -> url.contains("self-service"), new Page.WaitForURLOptions().setTimeout(45000));
            Assertions.assertTrue(
                    page.url().contains("self-service"),
                    "Not redirected on Self Service Diagnostics page");
            log.info("Navigated to Self Service Diagnostics page");
        } catch (Exception e) {
            log.error("Navigation to Self Service Diagnostics failed: {}", e.getMessage());
            Assertions.fail("Navigation to Self Service Diagnostics failed: " + e.getMessage());
        }
    }

    @Test
    @Order(35)
    @QaseId(35)
    @QaseTitle("Upload File in Self Service Diagnostics")
    public void uploadFileInSSD() {
        try {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("  Upload File")).click();
            log.info("Upload File Button Clicked");

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

            page.waitForLoadState(LoadState.NETWORKIDLE);

            Locator fileInput = page.locator("input[type='file']");

            fileInput.waitFor(new Locator.WaitForOptions().setTimeout(2000));

            fileInput.hover();
            log.info("File input hovered");
            page.evaluate("selector => document.querySelector(selector).style.display = 'block'", "input[type='file']");

            Path filePath = Paths.get("C:\\Users\\abhay\\Downloads\\Self Diagnostics test - Sheet1.csv");

            fileInput.setInputFiles(filePath.toAbsolutePath());
            page.waitForTimeout(1000);

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Upload").setExact(true)).click();
            log.info("File uploaded successfully!");

            page.waitForTimeout(1500);

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("  Refresh Status")).click();
            log.info("Refresh Status button clicked");

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
            page.waitForLoadState(LoadState.NETWORKIDLE);
        } catch (Exception e) {
            log.error("Upload File in Self Service Diagnostics failed: {}", e.getMessage());
            Assertions.fail("Upload File in Self Service Diagnostics failed: " + e.getMessage());
        }
    }

    @Test
    @Order(36)
    @QaseId(36)
    @QaseTitle("Navigate to Predictions")
    public void navigateToPredictions() {
        try {
            page.locator("body > app > default-layout > div > aside > div.sidebar.collapsed").hover();
            log.info("Sidebar hovered");

            page.waitForTimeout(2000);

            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Prediction")).click();
            log.info("Predictions clicked");

            page.waitForURL(url -> url.contains("predictions-v2"), new Page.WaitForURLOptions().setTimeout(45000));
            Assertions.assertTrue(page.url().contains("predictions-v2"), "Not redirected on Predictions page");
            log.info("Navigated to Predictions page");
        } catch (Exception e) {
            log.error("Navigation to Predictions failed: {}", e.getMessage());
            Assertions.fail("Navigation to Predictions failed: " + e.getMessage());
        }
    }
}