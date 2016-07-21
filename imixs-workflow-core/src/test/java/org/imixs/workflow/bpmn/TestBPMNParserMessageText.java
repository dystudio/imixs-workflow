package org.imixs.workflow.bpmn;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.ModelException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Test class test the Imixs BPMNParser
 * 
 * @author rsoika
 */
public class TestBPMNParserMessageText {

	@Before
	public void setup() {

	}

	@After
	public void teardown() {

	}

	@Test
	public void testSimple() throws ParseException,
			ParserConfigurationException, SAXException, IOException {

		String VERSION = "1.0.0";

		InputStream inputStream = getClass().getResourceAsStream(
				"/bpmn/message_example.bpmn");

		BPMNModel model = null;
		try {
			model = BPMNParser.parseModel(inputStream, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (ModelException e) {
			e.printStackTrace();
			Assert.fail();
		}
		Assert.assertNotNull(model);

		// Test Environment
		ItemCollection profile = model.getDefinition();
		Assert.assertNotNull(profile);
		Assert.assertEquals("environment.profile",
				profile.getItemValueString("txtname"));
		Assert.assertEquals("WorkflowEnvironmentEntity",
				profile.getItemValueString("type"));
		Assert.assertEquals(VERSION,
				profile.getItemValueString("$ModelVersion"));

		Assert.assertTrue(model.workflowGroups.contains("Message Example"));

		// test count of elements
		Assert.assertEquals(2, model.findTasks().size());

		// test task 1000
		ItemCollection task = model.getTask(1000);
		Assert.assertNotNull(task);
		Assert.assertEquals("1.0.0", task.getItemValueString("$ModelVersion"));
		Assert.assertEquals("Message Example",
				task.getItemValueString("txtworkflowgroup"));

		// test activity for task 1000
		List<ItemCollection> activities = model.findEvents(1000
				);
		Assert.assertNotNull(activities);
		Assert.assertEquals(1, activities.size());

		// test activity 1000.10 submit
		ItemCollection activity = model.getEvent(1000, 10);
		Assert.assertNotNull(activity);

		Assert.assertEquals("Some MessageMessage-Text",
				activity.getItemValueString("txtmailsubject"));

		String message = activity.getItemValueString("rtfMailBody");

		Assert.assertEquals(
				"<h1>Some Message Text</h1>\nThis is some message\nMessage-Text",
				message);

	}

}
