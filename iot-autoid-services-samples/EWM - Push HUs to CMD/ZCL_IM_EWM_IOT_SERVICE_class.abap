class ZCL_IM_EWM_IOT_SERVICE definition
  public
  final
  create public .

public section.

  interfaces IF_EX_EXEC_METHODCALL_PPF .
protected section.
private section.

  methods ANNOUNCE_HU_TO_IOT
    importing
      !IO_DLV_PPF type ref to /SCDL/CL_DLV_PPF
      !IS_DOCID type /SCWM/DLV_DOCID_ITEM_STR
      !IP_APPLICATION_LOG type BALLOGHNDL
      !IP_DISPATCH type PPFDDSPTCH
      !IP_FLT_VAL type PPFDFLTVAL
    returning
      value(RP_STATUS) type PPFDTSTAT .
ENDCLASS.



CLASS ZCL_IM_EWM_IOT_SERVICE IMPLEMENTATION.


* <SIGNATURE>---------------------------------------------------------------------------------------+
* | Instance Private Method ZCL_IM_EWM_IOT_SERVICE->ANNOUNCE_HU_TO_IOT
* +-------------------------------------------------------------------------------------------------+
* | [--->] IO_DLV_PPF                     TYPE REF TO /SCDL/CL_DLV_PPF
* | [--->] IS_DOCID                       TYPE        /SCWM/DLV_DOCID_ITEM_STR
* | [--->] IP_APPLICATION_LOG             TYPE        BALLOGHNDL
* | [--->] IP_DISPATCH                    TYPE        PPFDDSPTCH
* | [--->] IP_FLT_VAL                     TYPE        PPFDFLTVAL
* | [<-()] RP_STATUS                      TYPE        PPFDTSTAT
* +--------------------------------------------------------------------------------------</SIGNATURE>
  METHOD announce_hu_to_iot.

    DATA: lv_msg_txt   TYPE string.                         "#EC NEEDED

    DATA: lv_qname  TYPE trfcqnam,
          lv_guid   TYPE guid,

          lv_xhu    TYPE boole_d,
          lv_status TYPE /scdl/dl_status_type.

* Query
    DATA: lt_docid        TYPE /scwm/dlv_docid_item_tab,
          ls_docid        TYPE /scwm/dlv_docid_item_str,
          ls_include_data TYPE /scwm/dlv_query_incl_str_prd,
          ls_read_options TYPE /scwm/dlv_query_contr_str,
          ls_whr_headers  TYPE /scwm/dlv_header_out_prd_str,
          lt_whr_headers  TYPE /scwm/dlv_header_out_prd_tab,
          lt_whr_items    TYPE /scwm/dlv_item_out_prd_tab,
          lt_huhdr        TYPE /scwm/tt_huhdr_int,
          lt_huitm        TYPE /scwm/tt_huitm_int,
          ls_status       TYPE /scdl/dl_status_str.

    DATA: lo_prd             TYPE REF TO /scwm/cl_dlv_management_prd.

    DATA: lv_docid           TYPE char100.

    FIELD-SYMBOLS: <hu>      TYPE /scwm/s_huhdr_int.

    ls_docid = is_docid.
    APPEND ls_docid TO lt_docid.

    ls_read_options-data_retrival_only      = abap_true.
    ls_read_options-mix_in_object_instances =
      /scwm/if_dl_c=>sc_mix_in_load_instance.

    ls_include_data-head_status     = abap_true.
    ls_include_data-head_status_dyn = abap_true.

    TRY.
        CALL FUNCTION '/SCWM/WHR_QUERY'
          EXPORTING
            it_docid        = lt_docid
            iv_doccat_whr   = ls_docid-doccat
            is_read_options = ls_read_options
            is_include_data = ls_include_data
          IMPORTING
            et_whr_headers  = lt_whr_headers
            et_whr_items    = lt_whr_items
            et_huhdr        = lt_huhdr
            et_huitm        = lt_huitm.

      CATCH /scdl/cx_delivery.
*     Handled by the following IF statement
    ENDTRY.

    IF lt_huhdr IS INITIAL.
      RETURN.
    ENDIF.

    CALL METHOD zewm_cl_iot_announce_srvc=>announce_pdi_hu
      EXPORTING
        iv_lgnum     = /scwm/cl_tm=>sv_lgnum
        iv_flt_val   = ip_flt_val
        it_whr_items = lt_whr_items
        it_huhdr     = lt_huhdr
        it_huitm     = lt_huitm
      RECEIVING
        rp_status    = rp_status.

  ENDMETHOD.


* <SIGNATURE>---------------------------------------------------------------------------------------+
* | Instance Public Method ZCL_IM_EWM_IOT_SERVICE->IF_EX_EXEC_METHODCALL_PPF~EXECUTE
* +-------------------------------------------------------------------------------------------------+
* | [--->] FLT_VAL                        TYPE        PPFDFLTVAL
* | [--->] IO_APPL_OBJECT                 TYPE REF TO OBJECT
* | [--->] IO_PARTNER                     TYPE REF TO CL_PARTNER_PPF
* | [--->] IP_APPLICATION_LOG             TYPE        BALLOGHNDL
* | [--->] IP_PREVIEW                     TYPE        CHAR1
* | [--->] II_CONTAINER                   TYPE REF TO IF_SWJ_PPF_CONTAINER
* | [--->] IP_ACTION                      TYPE        PPFDTT(optional)
* | [<-()] RP_STATUS                      TYPE        PPFDTSTAT
* +--------------------------------------------------------------------------------------</SIGNATURE>
  METHOD if_ex_exec_methodcall_ppf~execute.
    DATA:
      lo_dlv_ppf     TYPE REF TO /scdl/cl_dlv_ppf,
      ls_docid       TYPE /scwm/dlv_docid_item_str,
      lo_ppf         TYPE REF TO cl_manager_ppf,
      " processing time
      l_trigger_guid TYPE guid_32,
      lo_trigger     TYPE REF TO cl_trigger_ppf,
      l_os_guid      TYPE os_guid,
      lv_dispatch    TYPE ppfddsptch,

      lo_af          TYPE REF TO /scdl/cl_af_management,
      lo_af_tm       TYPE REF TO /scwm/cl_adapter_tm,
      lo_tm          TYPE REF TO /scwm/if_tm,
      lo_tm_trace    TYPE REF TO /scwm/if_tm_trace,
      lv_trace_par   TYPE symsgv,

      lv_msg_txt     TYPE string ##NEEDED.

    " No processing for 'Preview'
    CHECK ip_preview IS INITIAL.

* Access persistent application data
    TRY.
        lo_dlv_ppf ?= io_appl_object.
      CATCH  cx_sy_move_cast_error cx_os_object_not_found.
        ASSERT ID /scwm/ppf CONDITION 1 = 0.
        MESSAGE e015(/scwm/delivery) INTO lv_msg_txt.
        cl_log_ppf=>add_message(
            ip_problemclass = sppf_pclass_1
            ip_handle       = ip_application_log ).
        rp_status = sppf_status_error.
        RETURN.
    ENDTRY.

    BREAK-POINT ID /scwm/ppf.


* Determine processing time (immediately, when saving, report)
    GET PARAMETER ID 'TRIGGERGUID' FIELD l_trigger_guid.

    IF l_trigger_guid IS NOT INITIAL.
      l_os_guid = l_trigger_guid.
      TRY.
          lo_trigger ?=
           ca_trigger_ppf=>agent->if_os_ca_persistency~get_persistent_by_oid(
                                  l_os_guid ).
        CATCH cx_sy_move_cast_error.
*       do nothing
      ENDTRY.

      IF NOT lo_trigger IS INITIAL.
        lv_dispatch = lo_trigger->get_dispatch( ).
      ENDIF.
    ENDIF.


* Control save for dispatch time 'report'
    IF lv_dispatch = sppf_process_later.   " report
      " set that PPF save BADI should be used to save instead of normal save.
      /scdl/cl_im_trig_exec=>sv_call_save_after_execution = abap_true.
      " set prohibited flag, as application needs to decide later if a
      " real save should be done.
      /scdl/cl_im_trig_exec=>sv_save_prohibited           = abap_true.
    ENDIF.

* get doccat, docid
    TRY.
        ls_docid-docid  = lo_dlv_ppf->get_docid( ).
        ls_docid-doccat = lo_dlv_ppf->get_doccat( ).
      CATCH cx_os_object_not_found
            cx_sy_move_cast_error.                      "#EC NO_HANDLER
        " Handled by the following IF statement
        ASSERT ID /scwm/ppf CONDITION 1 = 0.
    ENDTRY.

    IF ls_docid-docid  IS INITIAL OR
       ls_docid-doccat IS INITIAL.
      MESSAGE e015(/scwm/delivery) INTO lv_msg_txt.
      cl_log_ppf=>add_message(
          ip_problemclass = sppf_pclass_1
          ip_handle       = ip_application_log ).
      rp_status = sppf_status_error.
      RETURN.
    ENDIF.


* set warehouse for LUW and connect to transaction trace
    lo_ppf  = cl_manager_ppf=>get_instance( ).
    lo_tm ?= /scwm/cl_tm_factory=>get_service(
        /scwm/cl_tm_factory=>sc_manager ).
    lo_tm_trace ?= /scwm/cl_tm_factory=>get_service(
        /scwm/cl_tm_factory=>sc_trace ).

    IF lo_ppf->execution_mode = sppf_execute_report OR
       lo_ppf->execution_mode = sppf_execute_arfc.

      lo_af = /scdl/cl_af_management=>get_instance( ).
      lo_af_tm ?= lo_af->get_service(
         /scdl/if_af_management_c=>sc_trans_manager ).
      lo_af_tm->set_lgnum(
        EXPORTING
          iv_doccat = ls_docid-doccat
          iv_docid  = ls_docid-docid ).
      lo_tm->start_transaction(
          iv_ppfaction = ip_action ).

    ELSE.
      lv_trace_par = ip_action.
      lo_tm_trace->log_event(
          iv_event     = /scwm/if_tm_c=>sc_event_ppf_exec
          iv_param2    = lv_trace_par ).
    ENDIF.

    /scdl/cl_ppf_msg_log_map=>set_log_validity(
        iv_log  = ip_application_log
        iv_whno = /scwm/cl_tm=>sv_lgnum ).


    " start of actual action execution
    CASE flt_val.
      WHEN 'ZEWM_ANNOUNCE_HU'.

*|      Set Anounce
        rp_status = me->announce_hu_to_iot(
                                    EXPORTING
                                      io_dlv_ppf         = lo_dlv_ppf
                                      is_docid           = ls_docid
                                      ip_application_log = ip_application_log
                                      ip_dispatch        = lv_dispatch
                                      ip_flt_val         = flt_val ).


    ENDCASE.
  ENDMETHOD.
ENDCLASS.