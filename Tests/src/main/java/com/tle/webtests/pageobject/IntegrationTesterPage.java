package com.tle.webtests.pageobject;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.tle.webtests.framework.PageContext;

public class IntegrationTesterPage extends AbstractPage<IntegrationTesterPage>
{
	private final String secret;
	private final String shareId;
	private final String signonUrl;
	private final String testerUrl;

	@FindBy(name = "username")
	private WebElement usernameField;
	@FindBy(name = "courseId")
	private WebElement courseIdField;
	@FindBy(name = "options")
	private WebElement optionsField;
	@FindBy(name = "action")
	private WebElement actionDropdown;
	@FindBy(name = "makeReturn")
	private WebElement makeReturn;
	@FindBy(name = "url")
	private WebElement signonUrlField;
	@FindBy(name = "sharedSecretId")
	private WebElement sharedSecretIdField;
	@FindBy(name = "sharedSecret")
	private WebElement sharedSecretField;
	@FindBy(name = "selectMultiple")
	private WebElement selectMultipleCheck;
	@FindBy(xpath = "//input[@type='submit']")
	private WebElement executeButton;
	@FindBy(name = "itemXml")
	private WebElement itemXml;
	@FindBy(name = "powerXml")
	private WebElement powerXml;
	@FindBy(xpath = "//input[@value='POST to this URL']")
	private WebElement inputPostToUrl;
	
	public IntegrationTesterPage(PageContext context, String shareId, String secret)
	{
		super(context, By.name("url"));
		this.shareId = shareId;
		this.secret = secret;
		this.signonUrl = context.getBaseUrl() + "signon.do";
		this.testerUrl = getUrl(context);
	}
	
	public static String getUrl(PageContext context)
	{
		return context.getTestConfig().getIntegTesterUrl();
	}

	@Override
	protected void loadUrl()
	{
		driver.get(testerUrl);
	}

	public String getSignonUrl(String action, String username, String courseId, String options, boolean selectMultiple)
	{
		signonUrlField.clear();
		signonUrlField.sendKeys(signonUrl);
		sharedSecretIdField.clear();
		sharedSecretIdField.sendKeys(shareId);
		sharedSecretField.clear();
		sharedSecretField.sendKeys(secret);
		usernameField.clear();
		usernameField.sendKeys(username);
		courseIdField.clear();
		courseIdField.sendKeys(courseId);
		optionsField.clear();
		optionsField.sendKeys(options);
		if( selectMultipleCheck.isSelected() != selectMultiple )
		{
			selectMultipleCheck.click();
		}
		new Select(actionDropdown).selectByValue(action);
		if( !makeReturn.isSelected() )
		{
			makeReturn.click();
		}
		executeButton.click();
		get();
		return driver.findElement(By.xpath("//a")).getAttribute("href");
	}

	public String getSignonUrl(String action, String username, String courseId, String options)
	{
		return getSignonUrl(action, username, courseId, options, false);
	}

	public void select(String action, String username, String courseId, String options)
	{
		select(action, username, courseId, options, false);
	}

	public void select(String action, String username, String courseId, String options, boolean selectMultiple)
	{
		driver.get(getSignonUrl(action, username, courseId, options, selectMultiple));
	}

	public void setItemXml(String value)
	{
		itemXml.clear();
		itemXml.sendKeys(value);
	}

	public void setPowerXml(String value)
	{
		powerXml.clear();
		powerXml.sendKeys(value);
	}

	public <T extends PageObject> T clickPostToUrlButton(WaitingPageObject<T> targetPage)
	{
		scrollToElement(inputPostToUrl);
		inputPostToUrl.click();
		return targetPage.get();
	}
}
