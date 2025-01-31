package com.publicissapient.kpidashboard.apis.fe;

import com.tremend.trmt.form.engine.service.service.FormEngineClientService;
import com.tremend.trmt.form.engine.service.service.FormEngineForwardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FormEngineForwardServiceImpl extends FormEngineForwardService {

    public FormEngineForwardServiceImpl(
            FormEngineClientService clientService
    ) {
        super(clientService);

    }
    @Override
    protected String getUserToImpersonate() {
        return "test";
    }


}
