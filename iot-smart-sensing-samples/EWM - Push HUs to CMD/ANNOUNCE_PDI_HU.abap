  METHOD announce_pdi_hu.

*|  Structure
    DATA:
      ls_iot_service     TYPE   zewm_iot_service,
      ls_hu_announce_iot TYPE   zewm_hu_announce_iot.

*|  Select IOT Settings
    SELECT SINGLE * FROM zewm_iot_service INTO ls_iot_service WHERE lgnum = iv_lgnum AND
                                                                    flt_val = iv_flt_val.

    IF sy-subrc <> 0.
      RETURN.
    ENDIF.

    LOOP AT it_huhdr ASSIGNING FIELD-SYMBOL(<fs_huhdr>).

      ls_hu_announce_iot-lgnum = iv_lgnum.
      ls_hu_announce_iot-huident = <fs_huhdr>-huident.
      SHIFT ls_hu_announce_iot-huident LEFT DELETING LEADING ' 0'.

      LOOP AT it_huitm ASSIGNING FIELD-SYMBOL(<fs_huitm>) WHERE guid_parent = <fs_huhdr>-guid_hu.

        READ TABLE it_whr_items ASSIGNING FIELD-SYMBOL(<fs_whr_items>) WITH KEY docid = <fs_huitm>-qdocid
                                                                                itemid = <fs_huitm>-qitmid.

        IF sy-subrc <> 0.
          CONTINUE.
        ENDIF.

        ls_hu_announce_iot-docno = <fs_whr_items>-docno.
        SHIFT ls_hu_announce_iot-docno LEFT DELETING LEADING ' 0'.

*|      Try
        TRY.
*|          Sending IoT Service
            CALL METHOD zewm_cl_iot_announce_srvc=>sending_iot_service
              EXPORTING
                iv_lgnum           = iv_lgnum
                is_iot_service     = ls_iot_service
                is_hu_announce_iot = ls_hu_announce_iot.
          CATCH zcx_hu_api. " HU API Load and Unload

*|          Action processed Error
            rp_status = sppf_status_error.
*|          Return
            RETURN.
        ENDTRY.

      ENDLOOP.
    ENDLOOP.

*|  Action processed OK
    rp_status = sppf_status_processed.

  ENDMETHOD.