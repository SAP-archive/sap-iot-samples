  METHOD get_name_mapping_announce.

    rt_name_mapping = VALUE #( ( abap = 'lgnum         ' json = 'Warehouse' )
                               ( abap = 'huident    ' json = 'HandlingUnitExternalID' )
                               ( abap = 'docno        ' json = 'HandlingUnitReferenceDocument' )
                             ) ##NO_TEXT.
  ENDMETHOD.