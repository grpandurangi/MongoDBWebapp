package com.journaldev.app.servlets;

import java.io.IOException;
import java.util.List;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.journaldev.mongodb.dao.DAOException;
import com.journaldev.mongodb.dao.MongoDBPersonDAO;
import com.journaldev.mongodb.model.Person;
import com.journaldev.mongodb.model.Person.OPERATION;
import com.mongodb.MongoClient;

/**
 *  * Servlet implementation class FrontController
 *   */

public class FrontController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private MongoDBPersonDAO personDAO;
       
    /**
 *      * @throws JMSException 
 *           * @throws NamingException 
 *                * @throws IOException 
 *                     * @see HttpServlet#HttpServlet()
 *                          */
    public FrontController() throws IOException, NamingException, JMSException {
        super();
    }
    
    @Override
    public void init() throws ServletException {
    	super.init();
        ServletContext ctx = getServletContext();
        MongoClient mongo = (MongoClient) ctx.getAttribute("MONGO_CLIENT");
		personDAO = new MongoDBPersonDAO(mongo);
    }

	/**
 * 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 * 	 	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI();
		String target = "index.jsp";
		
		if(uri.endsWith("addPersonPage.do")) {
			target = "/addPerson.jsp";
		}
		if(uri.endsWith("addPerson.do")) {
			try {
				target = sendAddPersonDetail(request, response);
			} catch (DAOException e) {
				e.printStackTrace();
				target = "/addPerson.jsp";
				request.setAttribute("error", "Unable to add person.");
			}
		}
		if(uri.endsWith("editPersonPage.do")) {
			String id = request.getParameter("id");
			if (id == null || "".equals(id)) {
				throw new ServletException("id missing for edit operation");
			}
			System.out.println("Person edit requested with id=" + id);
			Person p = new Person();
			p.setId(id);
			try {
				p = personDAO.readPerson(p);
				request.setAttribute("person", p);
				target = "/editPerson.jsp";
			} catch (DAOException e) {
				e.printStackTrace();
				target = "editPersonPage.do";
				request.setAttribute("error", "Unable to edit");
			}
		}
		if(uri.endsWith("editPerson.do")) {
			try {
				target = sendEditPersonDetail(request, response);
			} catch (DAOException e ) {
				e.printStackTrace();
				request.setAttribute("error", "Something went wrong");
			}
		}
		if(uri.endsWith("deletePerson.do")) {
			try {
				target = sendDelPersonDetail(request, response);
			} catch (DAOException e) {
				e.printStackTrace();
				request.setAttribute("error", "Unable to delete person.");
			}
		}
		if(uri.endsWith("viewPersonPage.do")) {
			List<Person> persons;
			try {
				persons = personDAO.readAllPerson();
				request.setAttribute("persons", persons);
			} catch (DAOException e) {
				e.printStackTrace();
				request.setAttribute("error", "Something went wrong");
			}
			target = "/viewPersons.jsp";	
		}
		if(uri.endsWith("searchPersonPage.do")) {
			target = "/searchPerson.jsp";
		}
		if(uri.endsWith("home.do")) {
			target = "/index.jsp";
		}
		if(uri.endsWith("updateCountryPage.do")) {
			target = "/updateCountryPage.jsp";
		}
		if(uri.endsWith("search.do")) {
			Person p = new Person();
			p.setName(request.getParameter("name"));
			try {
				p = personDAO.searchPerson(p);
				if(p == null) {
					request.setAttribute("error", "No data found");
				} else {
					request.setAttribute("person", p);
				}
			} catch (DAOException e) {
				request.setAttribute("error", "No data found");
			}
			target = "/searchPerson.jsp";
		}
		if(uri.endsWith("serverError.do")) {
			try {
				throw new Exception("Server error created to check Alert and Respond");
			} catch (Exception e) {
				throw new ServletException(e.getLocalizedMessage(), e);
			}
		}
		if(uri.endsWith("pageNotFound.do")) {
			target = "dummyPage.jsp";
		}
		RequestDispatcher rd = request.getRequestDispatcher(target);
		rd.forward(request, response);
	}
	
	private String sendAddPersonDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, DAOException {
		String name = request.getParameter("name");
		String country = request.getParameter("country");
		if ((name == null || name.equals(""))
				|| (country == null || country.equals(""))) {
			request.setAttribute("error", "Mandatory Parameters Missing");
			return "/addPerson.jsp";
		} else {
			Person p = new Person();
			p.setCountry(country);
			p.setName(name);
			p.setOperation(OPERATION.SAVE);
			
			personDAO.createPerson(p);
			System.out.println("Person added successfully with id=" + p.getId());
			
			request.setAttribute("success", "Person added successfully with id=" + p.getId());

			return "/index.jsp";
		}
	}
	
	private String sendDelPersonDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, DAOException {
		String id = request.getParameter("id");
		if (id == null || "".equals(id)) {
			throw new ServletException("id missing for delete operation");
		}
		Person p = new Person();
		p.setId(id);
		p.setOperation(OPERATION.DELETE);
		personDAO.deletePerson(p);
		System.out.println("Person deleted successfully with id=" + p.getId());
		request.setAttribute("success", "Person deleted successfully with id=" + p.getId());

		return "/index.jsp";
	}

	private String sendEditPersonDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, DAOException {
		
		String id = request.getParameter("id"); // keep it non-editable in UI
		if (id == null || "".equals(id)) {
			throw new ServletException("id missing for edit operation");
		}

		String name = request.getParameter("name");
		String country = request.getParameter("country");

		if ((name == null || name.equals(""))
				|| (country == null || country.equals(""))) {
			request.setAttribute("error", "Name and Country Can't be empty");
			Person p = new Person();
			p.setId(id);
			p.setName(name);
			p.setCountry(country);
			request.setAttribute("person", p);

			return "/editPerson.jsp";
		} else {
			Person p = new Person();
			p.setId(id);
			p.setName(name);
			p.setCountry(country);
			p.setOperation(OPERATION.EDIT);
			
			personDAO.updatePerson(p);
			System.out.println("Person updated successfully with id=" + p.getId());
			request.setAttribute("success", "Person updated successfully with id=" + p.getId());
			
			Person p2;
			try {
				p2 = personDAO.readPerson(p);
				request.setAttribute("person", p2);
				return "/searchPerson.jsp";
			} catch (DAOException e) {
				e.printStackTrace();
				request.setAttribute("error", "Something went wrong");
				return "/editPerson.jsp";
			}
		}
	}

	/**
 * 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 * 	 	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
