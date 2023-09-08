package com.tarento.upsmf.examsAndAdmissions.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Constants {

    // General
    public static final String MESSAGE = "message";
    public static final String SUCCESSFUL = "Successful";
    public static final String RESPONSE = "response";
    public static final String FAILUREMESSAGE = "failure message";
    public static final String SUCCESSMESSAGE = "Successfully stores the data";
    public static final String LOCAL_BASE_PATH = "/tmp/";
    public static final String FAILED = "Failed";
    public static final String API_USER_BULK_UPLOAD = "api.user.bulk.upload";
    public static final String API_FILE_UPLOAD = "api.file.upload";
    public static final String API_FILE_DOWNLOAD = "api.file.download";
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String API_VERSION_1 = "1.0";

    public static final int CLIENT_ERROR = 400;
    public static final int SERVER_ERROR = 500;
    public static final String UNAUTHORIZED = "Unauthorized";


    //Exam
    public static final String API_EXAM_ADD = "api.exam.add";
    public static final String API_EXAM_GET_ALL = "api.exam.get";
    public static final String API_EXAM_GET_BY_ID = "api.exam.getById";
    public static final String API_EXAM_DELETE = "api.exam.delete";
    public static final String API_EXAM_UPDATE = "api.exam.update";
    public static final String API_EXAM_RESTORE = "api.exam.restore";

    //Question Paper
    public static final String API_QUESTION_PAPER_GET_ALL = "api.questionPaper.get";
    public static final String API_QUESTION_PAPER_GET_BY_ID = "api.questionPaper.getById";
    public static final String API_QUESTION_PAPER_DELETE = "api.questionPaper.delete";
    public static final String API_QUESTION_PAPER_PREVIEW = "api.questionPaper.preview";


    // Payment
    public static final String API_PAYMENT_ADD = "api.payment.add";
    public static final String EXAM_CYCLE_ID = "examCycleId";
    public static final String EXAM_DATE = "examDate";
    public static final String COURSE_ID = "course_id";
    public static final String MODIFIED_BY = "modifiedBy";
    public static final String DATE_MODIFIED_ON = "modifiedOn";
    public static final String TOTAL_MARKS = "totalMarks";
    public static final String EXAM_CYCLE_NAME = "examCycleName";
    public static final String COURSE_NAME = "courseName";
    public static final String EXAM_NAME = "examName";

    public interface Exception {
        String EXCEPTION_METHOD = "Exception in method %s : %s";
        String GET_NODE_ERROR = "Unable to get the node";
    }

    public class ResponseCodes {
        private ResponseCodes() {
        }

        public static final int UNAUTHORIZED_ID = 401;
        public static final int SUCCESS_ID = 200;
        public static final int FAILURE_ID = 320;
        public static final String UNAUTHORIZED = "Invalid credentials. Please try again.";
        public static final String PROCESS_FAIL = "Process failed, Please try again.";
        public static final String SUCCESS = "success";
    }

    public class ServiceRepositories {
        private ServiceRepositories() {
        }

        public static final String NOTIFICATION_UTIL = "notificationUtil";
    }

    public interface Parameters {
        String ID = "id";
        String AUTHORIZATION = "Authorization";
        String X_USER_TOKEN = "x-authenticated-user-token";
        String RESPONSE = "response";
        String IS_DETAIL = "isDetail";
        String PARENT_ID = "parentId";
        String CHILD = "child";
        String NAME = "name";
        String DESCRIPTION = "description";
        String STATUS = "status";
        String SOURCE = "source";
        String LEVEL = "level";
        String DATA = "data";
        String RESULT = "result";
        String ROOT_ORG = "rootOrg";
        String ORG = "org";
        String USER_ID = "userId";

        String COMPETENCY = "COMPETENCY";
        String KNOWLEDGERESOURCE = "KNOWLEDGERESOURCE";
        String COMPETENCIESLEVEL = "COMPETENCIESLEVEL";
        String COMPETENCYAREA = "COMPETENCYAREA";

        String ANONYMOUS = "Anonymous";
        String UNAUTHORIZED = "Unauthorized";
        String DOT_SEPARATOR = ".";
        String SHA_256_WITH_RSA = "SHA256withRSA";
        String SUB = "sub";
        String ISS = "iss";
        String EXP = "exp";
        String KID = "kid";
    }

    public interface Actions {
        String CREATE = "CREATED";
        String UPDATE = "UPDATED";
        String REMOVE = "REMOVED";
    }

    public interface WorkflowState {
        String SERVICE = "entity";
        String INITIATE = "INITIATE";
        String UNVERIFIED = "UNVERIFIED";
        String DRAFT = "DRAFT";
    }

    public static final String IDENTIFIER = "identifier";
    public static final String FILE_NAME = "fileName";
    public static final String GCP_FILE_NAME = "gcpFileName";
    public static final String GCP_FILE_NAME_QUERY = "SELECT gcp_file_name FROM public.question_paper WHERE id=?;";
    public static final String FILE_PATH = "filePath";
    public static final String DATE_CREATED_ON = "dateCreatedOn";
    public static final String DATE_UPDATE_ON = "dateUpdatedOn";
    public static final String INITIATED_CAPITAL = "INITIATED";
    public static final String STATUS = "status";
    public static final String COMMENT = "comment";
    public static final String CREATED_BY = "createdBy";

    public static final String EMAIL = "email";

    public static final String FIRST_NAME = "first_name";

    public static final String UUID = "wid";

    public static final String ROOT_ORG_CONSTANT = "rootOrg";

    public static final List<String> USER_DEFAULT_FIELDS = Collections
            .unmodifiableList(Arrays.asList(UUID, FIRST_NAME, EMAIL));
}
