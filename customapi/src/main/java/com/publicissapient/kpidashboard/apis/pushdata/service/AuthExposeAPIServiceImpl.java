package com.publicissapient.kpidashboard.apis.pushdata.service;

import java.time.LocalDate;
import java.util.Objects;

import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.pushdata.model.ExposeApiToken;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.ExposeAPITokenRequestDTO;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.ExposeAPITokenResponseDTO;
import com.publicissapient.kpidashboard.apis.pushdata.repository.ExposeApiTokenRepository;
import com.publicissapient.kpidashboard.common.util.Encryption;
import com.publicissapient.kpidashboard.common.util.EncryptionException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthExposeAPIServiceImpl implements AuthExposeAPIService {

	private static final long TOKEN_EXPIRY_DAYS = 30L;

	@Autowired
	private ExposeApiTokenRepository exposeApiTokenRepository;
	final ModelMapper modelMapper = new ModelMapper();

	/**
	 * only one generate token per project and user wise, if user generate token
	 * again for same then existing token and expiry will be updated.
	 * 
	 * @param exposeAPITokenRequestDTO
	 * @return
	 */
	@Override
	public ServiceResponse generateAndSaveToken(ExposeAPITokenRequestDTO exposeAPITokenRequestDTO) {
		ExposeAPITokenResponseDTO exposeAPITokenResponseDTO = new ExposeAPITokenResponseDTO();
		ExposeApiToken exposeApiTokenExist = exposeApiTokenRepository.findByUserNameAndBasicProjectConfigId(
				exposeAPITokenRequestDTO.getUserName(),
				new ObjectId(exposeAPITokenRequestDTO.getBasicProjectConfigId()));
		String apiAccessToken = "";
		try {
			apiAccessToken = Encryption.getStringKey();
			if (Objects.nonNull(exposeApiTokenExist)) {
				exposeApiTokenExist.setApiToken(apiAccessToken);
				exposeApiTokenExist.setExpiryDate(LocalDate.now().plusDays(TOKEN_EXPIRY_DAYS));
				exposeApiTokenExist.setUpdatedAt(LocalDate.now());
				exposeApiTokenRepository.save(exposeApiTokenExist);
				exposeAPITokenResponseDTO = modelMapper.map(exposeApiTokenExist, ExposeAPITokenResponseDTO.class);
				return new ServiceResponse(true, "API token Is updated , after onward use this token",
						exposeAPITokenResponseDTO);
			} else {
				ExposeApiToken exposeApiTokenNew = new ExposeApiToken();
				exposeApiTokenNew.setUserName(exposeAPITokenRequestDTO.getUserName());
				exposeApiTokenNew.setExpiryDate(LocalDate.now().plusDays(TOKEN_EXPIRY_DAYS));
				exposeApiTokenNew.setCreatedAt(LocalDate.now());
				exposeApiTokenNew
						.setBasicProjectConfigId(new ObjectId(exposeAPITokenRequestDTO.getBasicProjectConfigId()));
				exposeApiTokenNew.setProjectName(exposeAPITokenRequestDTO.getProjectName());
				exposeApiTokenNew.setApiToken(apiAccessToken);
				exposeApiTokenRepository.save(exposeApiTokenNew);
				exposeAPITokenResponseDTO = modelMapper.map(exposeApiTokenNew, ExposeAPITokenResponseDTO.class);
				return new ServiceResponse(true, "Please save this API token for API Call", exposeAPITokenResponseDTO);
			}
		} catch (EncryptionException e) {
			return new ServiceResponse(false, "Error while Creating token", null);
		}
	}
}
