package org.jobcenter.webservice;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.jobcenter.constants.WebServiceURLConstants;
import org.jobcenter.dto.Job;
import org.jobcenter.exception.RecordNotUpdatedException;
import org.jobcenter.request.*;
import org.jobcenter.response.*;
import org.jobcenter.service.*;
import org.jobcenter.service_response.SubmitJobServiceResponse;

import com.sun.jersey.spi.inject.Inject;
import com.sun.jersey.spi.resource.Singleton;



/**
 *
 *
 */
@Produces("application/xml")

@Path( WebServiceURLConstants.SUBMIT_JOB )

//  Jersey specific annotation
@Singleton
public class SubmitJobWebService {

	private static Logger log = Logger.getLogger(SubmitJobWebService.class);

	@Inject
	private SubmitJobService submitJobService;

	@Inject
	private GetJobForGUIService getJobForGUIService;
	
	/**
	 * @return
	 */
	@POST
	@Consumes("application/xml")
	public SubmitJobResponse submitJob( SubmitJobRequest submitJobRequest, @Context HttpServletRequest request ) {

		if ( log.isInfoEnabled() ) {
			log.info( "submitJob: getNodeName(): " + submitJobRequest.getNodeName()  );
		}
		
		if ( log.isDebugEnabled() ) {
			StringBuilder msgSB = new StringBuilder( 5000 );
			msgSB.append( "SubmitJobWebService: Before save the job: NodeName: |" );
			msgSB.append( submitJobRequest.getNodeName() );
			msgSB.append( "|, JobTypeName: |" );
			msgSB.append( submitJobRequest.getJobTypeName() );
			msgSB.append( "|, " );
			
			if ( submitJobRequest.getJobParameters() == null ) {
				msgSB.append( " Job Parameters Map is null " );
			} else if ( submitJobRequest.getJobParameters().isEmpty() ) {
				msgSB.append( " Job Parameters Map is empty " );
			} else {
				msgSB.append( " Job Parameters: size: " + submitJobRequest.getJobParameters().size() 
						+ " [ " );
				for ( Map.Entry<String, String> entry : submitJobRequest.getJobParameters().entrySet() ) {
					msgSB.append( "key: |" );
					msgSB.append( entry.getKey() );
					msgSB.append( "|, value: |" );
					msgSB.append( entry.getValue() );
					msgSB.append( "|," );
				}
				msgSB.append( " ] " );	
			}
			String msg = msgSB.toString();
			log.debug( msg );
		}

		String remoteHost = request.getRemoteHost();


//		int remotePort = request.getRemotePort();

		int errorCode = JobResponse.ERROR_CODE_NO_ERRORS;


		try {
			SubmitJobServiceResponse submitJobServiceResponse =
					submitJobService.submitJob( submitJobRequest, remoteHost );
			

			if ( log.isDebugEnabled() ) {
				StringBuilder msgSB = new StringBuilder( 5000 );
				msgSB.append( "SubmitJobWebService: AFTER save the job: Saved Job ID: " );
				msgSB.append( Integer.toString( submitJobServiceResponse.getJobId() ) );
				msgSB.append(  ", NodeName: |" );
				msgSB.append( submitJobRequest.getNodeName() );
				msgSB.append( "|, JobTypeName: |" );
				msgSB.append( submitJobRequest.getJobTypeName() );
				msgSB.append( "|, " );
				
				if ( submitJobRequest.getJobParameters() == null ) {
					msgSB.append( " Job Parameters Map is null " );
				} else if ( submitJobRequest.getJobParameters().isEmpty() ) {
					msgSB.append( " Job Parameters Map is empty " );
				} else {
					msgSB.append( " Job Parameters: size: " + submitJobRequest.getJobParameters().size() 
							+ " [ " );
					for ( Map.Entry<String, String> entry : submitJobRequest.getJobParameters().entrySet() ) {
						msgSB.append( "key: |" );
						msgSB.append( entry.getKey() );
						msgSB.append( "|, value: |" );
						msgSB.append( entry.getValue() );
						msgSB.append( "|," );
					}
					msgSB.append( " ] " );	
				}
				String msg = msgSB.toString();
				log.debug( msg );
			}

			
			SubmitJobResponse submitJobResponse = submitJobServiceResponse.getSubmitJobResponse();
			
			if ( ! submitJobResponse.isErrorResponse() ) {
				
				int insertedJobId = submitJobServiceResponse.getJobId();
				
				if ( log.isDebugEnabled() ) {
				
					ViewJobRequest viewJobRequest = new ViewJobRequest();
					
					viewJobRequest.setJobId(insertedJobId);
					viewJobRequest.setClientIdentifierDTO( submitJobRequest.getClientIdentifierDTO() );
					viewJobRequest.setNodeName( submitJobRequest.getNodeName() );
					
					ViewJobResponse viewJobResponse =
							getJobForGUIService.retrieveJob( viewJobRequest, remoteHost );
				
					Job jobRetrievedFromDB = viewJobResponse.getJob();
					
					StringBuilder msgSB = new StringBuilder( 5000 );
					msgSB.append( "SubmitJobWebService: Inserted job retrieved from DB: " );
					msgSB.append(  " NodeName: |" );
					msgSB.append( submitJobRequest.getNodeName() );
					msgSB.append( "|, JobTypeName: |" );
					msgSB.append( submitJobRequest.getJobTypeName() );
					msgSB.append( "|.  Data from job object: Saved Job ID: " );
					msgSB.append( Integer.toString( jobRetrievedFromDB.getId() ) );
					msgSB.append( ", " );
					msgSB.append(  "Request ID on Job object: " );
					msgSB.append( Integer.toString( jobRetrievedFromDB.getRequestId() ) );
					msgSB.append(  ", " );

					msgSB.append( "job object JobParameterCount: " );
						msgSB.append( Integer.toString( jobRetrievedFromDB.getJobParameterCount() ) );
						msgSB.append( ", " );
						
					if ( jobRetrievedFromDB.getJobParameters() == null ) {
						msgSB.append( " Job Parameters Map is null " );
					} else if ( jobRetrievedFromDB.getJobParameters().isEmpty() ) {
						msgSB.append( " Job Parameters Map is empty " );
					} else {
						msgSB.append( " Job Parameters Map: size: " + jobRetrievedFromDB.getJobParameters().size() 
								+ " [ " );
						for ( Map.Entry<String, String> entry : jobRetrievedFromDB.getJobParameters().entrySet() ) {
							msgSB.append( "key: |" );
							msgSB.append( entry.getKey() );
							msgSB.append( "|, value: |" );
							msgSB.append( entry.getValue() );
							msgSB.append( "|," );
						}
						msgSB.append( " ] " );	
					}
					String msg = msgSB.toString();
					log.debug( msg );
					
				}
			}

			return submitJobResponse;

		} catch (RecordNotUpdatedException e) {
			log.error( "submitJob Failed: RecordNotUpdatedException: Exception: JobWebService:: updateJobStatus:   getNodeName(): " + submitJobRequest.getNodeName() + ", Exception: " + e.toString() , e );
			errorCode = JobResponse.ERROR_CODE_DATABASE_NOT_UPDATED;
		} catch (Throwable e) {
			log.error( "submitJob Failed: Exception: JobWebService:: updateJobStatus:   getNodeName(): " + submitJobRequest.getNodeName() + ", Exception: " + e.toString() , e );
			errorCode = JobResponse.ERROR_CODE_GENERAL_ERROR;
		}

		SubmitJobResponse submitJobResponse = new SubmitJobResponse();
		submitJobResponse.setErrorResponse( true );
		submitJobResponse.setErrorCode( errorCode );
		submitJobResponse.setClientIPAddressAtServer( remoteHost );

		return submitJobResponse;
	}

}