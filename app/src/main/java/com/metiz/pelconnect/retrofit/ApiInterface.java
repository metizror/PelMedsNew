package com.metiz.pelconnect.retrofit;

import com.metiz.pelconnect.model.ChangedPrescriptionModel;
import com.metiz.pelconnect.model.DownloadStatusModel;
import com.metiz.pelconnect.model.DownloadSuccessRequestModel;
import com.metiz.pelconnect.model.GetCompleteDoseModel;
import com.metiz.pelconnect.model.GetGivenMedPassPrescriptionModel;
import com.metiz.pelconnect.model.ImageUploadModel;
import com.metiz.pelconnect.model.InsertFillPackhistoryModel;
import com.metiz.pelconnect.model.LoginModel;
import com.metiz.pelconnect.model.MedPassPrnDetailModel;
import com.metiz.pelconnect.model.MedPillMultiMedpassModel;
import com.metiz.pelconnect.model.MedSheetDownloadReportModel;
import com.metiz.pelconnect.model.MedpassPatientListModel;
import com.metiz.pelconnect.model.MedsheetUrlModel;
import com.metiz.pelconnect.model.MultiPackModel;
import com.metiz.pelconnect.model.ReminderNotificationModel;
import com.metiz.pelconnect.model.UpdatePatientMedPassModel;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiInterface {

    @POST("Login")
    Call<LoginModel> makeLogin(@Body RequestBody loginRequest);

    @POST("UpdatePatientMedSheetDownloadStatus")
    Call<DownloadStatusModel> UpdatePatientMedSheetDownloadStatus(@Body List<DownloadSuccessRequestModel> downloadSuccessRequestModelList);

    @POST("UpdatePatientMedPassDrugDetails")
    Call<UpdatePatientMedPassModel> UpdatePatientMedPassDrugDetails(@Body RequestBody UpdatePatientMedPassDrugRequest);

    @POST("InsertFillPackhistory")
    Call<InsertFillPackhistoryModel> InsertFillPackhistory(@Body RequestBody UpdatePatientMedPassDrugRequest);

    @POST("GetMedPasspatientList_12_July_2022")
    Call<MedpassPatientListModel> getPatientList(@Query("FacilityID") int FacilityID, @Query("currdate") String currdate);

   @POST("GetCompleteDose")
    Call<GetCompleteDoseModel> GetCompleteDose(@Query("FacilityID") int FacilityID, @Query("PatientID") int PatientID, @Query("DoseTime") String DoseTime);

    @POST("GetMedGivenpatientList")
    Call<MedpassPatientListModel> getGivenPatientList(@Query("FacilityID") int FacilityID, @Query("currdate") String currdate);

    @GET("GetCurrentMedpassDetailsByFillid")
    Call<MedPillMultiMedpassModel>
    GetCurrentMedpassDetailsByFillid(@Query("FacilityID") int FacilityID, @Query("ReceivedPatientID") int ReceivedPatientID, @Query("FillID") String FillID, @Query("Dosedate") String Dosedate, @Query("DoseTime") String DoseTime);

    @POST("GetNo_OfPendingFillPack")
    Call<MultiPackModel> GetMultiPackCountData(@Query("FacilityID") int FacilityID, @Query("PatientID") int PatientID, @Query("FillID") String FillID);

    @POST("GetGivenMedPassDrugDetailsByPatientID")
    Call<GetGivenMedPassPrescriptionModel> GetGivenMedPassDrugDetailsByPatientID(@Query("PatientId") int PatientId, @Query("FacilityID") int FacilityID, @Query("currdate") String currdate);

    @POST("GetGivenMedPassDrugHistoryByPatientID")
    Call<GetGivenMedPassPrescriptionModel> GetGivenMedPassDrugHistoryByPatientID(@Query("PatientId") int PatientId, @Query("FacilityID") int FacilityID, @Query("currdate") String currdate);

    @POST("GetMedPassPRNDrugDetailsByPatientID_13_July_2022")
    Call<MedPassPrnDetailModel> GetMedPassPRNDrugDetailsByPatientID(@Query("PatientId") int PatientId);


    @POST("GetMissdoseNotificationByPatientID")
    Call<ReminderNotificationModel> GetMissdoseNotificationByPatientID(@Query("FacilityID") int FacilityID, @Query("UserID") String UserID, @Query("PatientID") String PatientID, @Query("DoseDate") String DoseDate, @Query("DoseTime") String DoseTime, @Query("Emailtype") String Emailtype);


    @POST("GetChangedPrescriptionByPatient_14_July_2022")
    Call<ChangedPrescriptionModel> GetChangedPrescriptionByPatient(@Query("patientid") int patientid);

    @GET("GetPatientMedSheetDownloadReport")
    Call<MedSheetDownloadReportModel> getPatientMedSheetDownloadReport(@Query("UserID") String UserID, @Query("Device_id") String Device_id);

/*
    @Multipart
    @POST("MedpassFormdataImageSave")
    Call<ImageUploadModel> medpassFormDataImageSave(
            @Part("FacilityID") RequestBody FacilityID,
            @Part("PatientID") RequestBody PatientID,
            @Part("DoseTime") RequestBody DoseTime,
            @Part("DoseDate") RequestBody DoseDate,
            @Part MultipartBody.Part file1,
            @Part MultipartBody.Part file2);
*/

    /*
        @POST("GetMedPassPatientList")
        Call<HolidayMasterModel> getBanner();
    */

    @POST("GetPatientMedsheetReport")
    Call<MedsheetUrlModel> getMedsheeturl(@Query("FacilityName") String FacilityName, @Query("ddlmonth") String ddlmonth, @Query("ddlYear") String ddlYear, @Query("PatientID") String PatientID);
}
