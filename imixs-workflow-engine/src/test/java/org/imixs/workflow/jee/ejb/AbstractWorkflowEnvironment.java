package org.imixs.workflow.jee.ejb;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.SessionContext;
import javax.xml.parsers.ParserConfigurationException;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.Model;
import org.imixs.workflow.WorkflowContext;
import org.imixs.workflow.bpmn.BPMNModel;
import org.imixs.workflow.bpmn.BPMNParser;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xml.sax.SAXException;

/**
 * Abstract base class for jUnit tests using the WorkflowService.
 * 
 * This test class mocks a complete workflow environment without the class
 * WorkflowService
 * 
 * The test class generates a test database with process entities and activity
 * entities which can be accessed from a plug-in or the workflowKernel.
 * 
 * A JUnit Test can save, load and process workitems.
 * 
 * JUnit tests can also manipulate the model by changing entities through
 * calling the methods:
 * 
 * getActivityEntity,setActivityEntity,getProcessEntity,setProcessEntity
 * 
 * 
 * @version 2.0
 * @see AbstractPluginTest, TestWorkflowService
 * @author rsoika
 */
public class AbstractWorkflowEnvironment {
	private final static Logger logger = Logger.getLogger(AbstractWorkflowEnvironment.class.getName());
 
	Map<String, ItemCollection> database = null;

	protected EntityService entityService;
	protected ModelService modelService;
	protected SessionContext ctx;
	protected WorkflowContext workflowContext;
	private Model model=null;


	@Before
	public void setup() throws PluginException {

		// setup db
		createTestDatabase();

		// mock EJBs and inject them into the workflowService EJB
		entityService = Mockito.mock(EntityService.class);
		modelService = Mockito.mock(ModelService.class);
		ctx = Mockito.mock(SessionContext.class);

		workflowContext = Mockito.mock(WorkflowContext.class);

		 model=loadModel("/bpmn/plugin-test.bpmn");

	
		// Simulate fineProfile("1.0.0") -> entityService.load()...
		when(entityService.load(Mockito.anyString())).thenAnswer(new Answer<ItemCollection>() {
			@Override
			public ItemCollection answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				String id = (String) args[0];
				ItemCollection result = database.get(id);
				return result;
			}
		});

		// simulate save() method
		when(entityService.save(Mockito.any(ItemCollection.class))).thenAnswer(new Answer<ItemCollection>() {
			@Override
			public ItemCollection answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				ItemCollection entity = (ItemCollection) args[0];
				database.put(entity.getItemValueString(EntityService.UNIQUEID), entity);
				return entity;
			}
		});

		// simulate SessionContext ctx.getCallerPrincipal().getName()
		Principal principal = Mockito.mock(Principal.class);
		when(principal.getName()).thenReturn("manfred");
		when(ctx.getCallerPrincipal()).thenReturn(principal);

		when(workflowContext.getModelManager()).thenReturn(modelService);
		
		/*+
		// simulate getActivityEntity
		when(modelService.getActivityEntity(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString()))
				.thenAnswer(new Answer<ItemCollection>() {
					@Override
					public ItemCollection answer(InvocationOnMock invocation) throws Throwable {
						Object[] args = invocation.getArguments();
						int p = (Integer) args[0];
						int a = (Integer) args[1];
						ItemCollection entity = database.get("A" + p + "-" + a);
						return entity;

					}
				});

		// simulate getActivityEntityList
		when(modelService.getActivityEntityList(Mockito.anyInt(), Mockito.anyString()))
				.thenAnswer(new Answer<List<ItemCollection>>() {
					@SuppressWarnings({ "rawtypes", "unused" })
					@Override
					public List<ItemCollection> answer(InvocationOnMock invocation) throws Throwable {
						ArrayList<ItemCollection> result = new ArrayList<ItemCollection>();
						Object[] args = invocation.getArguments();
						int p = (Integer) args[0];
						String m = (String) args[1];
						Iterator it = database.entrySet().iterator();
						while (it.hasNext()) {
							Map.Entry pair = (Map.Entry) it.next();
							if (pair.getKey().toString().startsWith("A" + p + "-")) {
								result.add((ItemCollection) pair.getValue());
							}
						}
						return result;

					}
				});
		
		
		
		// simulate getProcessEntity
		when(modelService.getProcessEntity(Mockito.anyInt(), Mockito.anyString()))
				.thenAnswer(new Answer<ItemCollection>() {
					@Override
					public ItemCollection answer(InvocationOnMock invocation) throws Throwable {
						Object[] args = invocation.getArguments();
						int p = (Integer) args[0];
						ItemCollection entity = database.get("P" + p);
						return entity;

					}
				});
		 */
	

	}

	/**
	 * Create a test database with some workItems and a simple model
	 */
	protected void createTestDatabase() {

		database = new HashMap<String, ItemCollection>();

		ItemCollection entity = null;
	
		logger.info("createSimpleDatabase....");
	
		
		// create workitems
		for (int i = 1; i < 6; i++) {
			entity = new ItemCollection();
			entity.replaceItemValue("type", "workitem");
			entity.replaceItemValue(EntityService.UNIQUEID, "W0000-0000" + i);
			entity.replaceItemValue("txtName", "Workitem " + i);
			entity.replaceItemValue("$ModelVersion", "1.0.0");
			entity.replaceItemValue("$ProcessID", 100);
			entity.replaceItemValue("$ActivityID", 10);
			entity.replaceItemValue(WorkflowService.ISAUTHOR, true);
			database.put(entity.getItemValueString(EntityService.UNIQUEID), entity);
		}

	}
	
	
	public Model getModel() {
		return model;
	}
	
	
	private Model loadModel(String path) {
		InputStream inputStream = getClass().getResourceAsStream(path);

		BPMNModel model = null;
		try {
			model = BPMNParser.parseModel(inputStream, "UTF-8");
		} catch (ModelException | ParseException | ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return model;
	}

}
