declare
  -- Local variables here
  l_script       varchar2(3200);
  l_trigger_name varchar2(128);

begin
  for r in (select all_cons_columns.owner,
                   all_cons_columns.table_name,
                   all_cons_columns.column_name
              from all_constraints, all_cons_columns
             where all_constraints.constraint_type = 'P'
               and all_constraints.constraint_name =
                   all_cons_columns.constraint_name
               and all_constraints.owner = all_cons_columns.owner
               and all_cons_columns.owner = 'WRITE_YOUR_DB_SCHEMA_NAME'
               and all_cons_columns.table_name not like 'BIN$%'
               and all_cons_columns.table_name != 'SYNC_TRANSFER'
             order by all_cons_columns.owner,
                      all_cons_columns.table_name,
                      all_cons_columns.position) loop
  
    l_trigger_name := 'T_' || r.table_name || '_I';

    -- end ; adjust trigger names for 30 character limit.
    l_script := 'CREATE OR REPLACE TRIGGER ' || r.owner || '.' ||
                l_trigger_name || '
    AFTER INSERT OR UPDATE OR DELETE
     ON ' || r.owner || '.' || r.table_name || '
     FOR EACH ROW

  DECLARE
     l_op_type varchar2(1);
     begin
      CASE
          WHEN INSERTING THEN
            l_op_type:= ''I'';
            INSERT INTO DB_SCHEMA_NAME.SYNC_TRANSFER
             ( id,
               table_owner,
               table_name,
               unique_key_statement,
               op_type,
               creation_date,
               transfer_date,
               transfer_status_id,
               transfer_status_desc )
             VALUES
             ( DB_SCHEMA_NAME.seq_sync_transfer.NEXTVAL,
               ''' || r.owner || ''',
               ''' || r.table_name || ''',
               ''' || r.column_name || '='' || :new.' ||
                        r.column_name || ',
               l_op_type,
               sysdate,
               null,
               ''PENDING'',
               ''PENDING'');

          WHEN UPDATING THEN
            l_op_type:= ''U'';
            INSERT INTO DB_SCHEMA_NAME.SYNC_TRANSFER
             ( id,
               table_owner,
               table_name,
               unique_key_statement,
               op_type,
               creation_date,
               transfer_date,
               transfer_status_id,
               transfer_status_desc )
             VALUES
             ( DB_SCHEMA_NAME.seq_sync_transfer.NEXTVAL,
               ''' || r.owner || ''',
               ''' || r.table_name || ''',
               ''' || r.column_name || '='' || :old.' ||
                        r.column_name || ',
               l_op_type,
               sysdate,
               null,
               ''PENDING'',
               ''PENDING'');

          WHEN DELETING THEN
            l_op_type:= ''D'';          
            INSERT INTO DB_SCHEMA_NAME.SYNC_TRANSFER
           ( id,
             table_owner,
             table_name,
             unique_key_statement,
             op_type,
             creation_date,
             transfer_date,
             transfer_status_id,
             transfer_status_desc )
           VALUES
           ( DB_SCHEMA_NAME.seq_sync_transfer.NEXTVAL,
             ''' || r.owner || ''',
             ''' || r.table_name || ''',
             ''' || r.column_name || '='' || :old.' ||
                      r.column_name || ',
             l_op_type,
             sysdate,
             null,
             ''PENDING'',
             ''PENDING'');

        END CASE;
      
  END;
  /
  
  ';
 
  dbms_output.put_line(l_script);
  end loop;

end;