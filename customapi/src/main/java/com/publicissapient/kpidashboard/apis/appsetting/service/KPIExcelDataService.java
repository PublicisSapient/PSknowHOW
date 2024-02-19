/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.appsetting.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.bitbucket.service.BitBucketServiceKanbanR;
import com.publicissapient.kpidashboard.apis.bitbucket.service.BitBucketServiceR;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.jenkins.service.JenkinsServiceKanbanR;
import com.publicissapient.kpidashboard.apis.jenkins.service.JenkinsServiceR;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceKanbanR;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.KPIExcelValidationDataResponse;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.sonar.service.SonarServiceKanbanR;
import com.publicissapient.kpidashboard.apis.sonar.service.SonarServiceR;
import com.publicissapient.kpidashboard.apis.zephyr.service.ZephyrService;
import com.publicissapient.kpidashboard.apis.zephyr.service.ZephyrServiceKanban;
import com.publicissapient.kpidashboard.common.model.application.KpiMaster;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.repository.application.KpiMasterRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Calls each collector tool service and collect the data. It also formats the
 * data in the required response structure.
 *
 * @author tauakram
 */
@Service
@Slf4j
public class KPIExcelDataService {

	private static final String KANBAN = "KANBAN";
	private static final String EXCEL_JIRA = "EXCEL-JIRA";
	private static final String EXCEL_JENKINS = "EXCEL-JENKINS";
	private static final String EXCEL_SONAR = "EXCEL-SONAR";
	private static final String EXCEL_BITBUCKET = "EXCEL-BITBUCKET";
	private static final String EXCEL_ZEPHYR = "EXCEL-ZEPHYR";
	private static final String EXCEL_JIRAKANBAN = "EXCEL-JIRAKANBAN";
	private static final String EXCEL_ZEPHYRKANBAN = "EXCEL-ZEPHYRKANBAN";
	private static final String EXCEL_SONARKANBAN = "EXCEL-SONARKANBAN";
	private static final String EXCEL_JENKINSKANBAN = "EXCEL-JENKINSKANBAN";
	private static final String EXCEL_BIBUCKETKANBAN = "EXCEL-BITBUCKETKANBAN";

	@Autowired
	private JiraServiceR jiraServiceR;

	@Autowired
	private JenkinsServiceR jenkinsServiceR;

	@Autowired
	private SonarServiceR sonarServiceR;

	@Autowired
	private ZephyrService zephyrService;

	@Autowired
	private BitBucketServiceR bitBucketServiceR;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private JiraServiceKanbanR jiraServiceKanbanR;

	@Autowired
	private ZephyrServiceKanban zephyrServiceKanban;

	@Autowired
	private SonarServiceKanbanR sonarServiceKanbanR;

	@Autowired
	private JenkinsServiceKanbanR jenkinsServiceKanbanR;

	@Autowired
	private BitBucketServiceKanbanR bitBucketServiceKanbanR;

	@Autowired
	private KpiMasterRepository kpiMasterRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	/**
	 * Processes the request for fetching the source wise KPI data. It leverages
	 * underneath custom API services to fetch data. If an exception occurred in any
	 * of source, the service returns data for sources it has processed so far. Why?
	 * Since the sources are independent instead of showing the exception, why not
	 * to show data of other sources which has been processed. How to know
	 * exceptions happening? Logs will detail the exceptions and if certain source
	 * data is not coming in response it may points to something happening.
	 * <p>
	 * The threads are not playing great role here. However, since this services
	 * fires query on level 1 and level 2 filters, which qualifies in the cached
	 * category, result will come fast after first hit.
	 *
	 * @param kpiID
	 *            the kpi id
	 * @param level
	 *            the level
	 * @param filterIds
	 *            the filter ids
	 * @param acceptedFilter
	 *            the accepted filter
	 * @param kpiRequest
	 *            the kpi request
	 * @param isKanban
	 *            the is kanban
	 * @return object
	 */

	public Object process(String kpiID, int level, List<String> filterIds, List<String> acceptedFilter,
			KpiRequest kpiRequest, Boolean isKanban) {

		Map<String, KpiRequest> kpiRequestSourceWiseMap = createKPIRequest(kpiID, level, filterIds, kpiRequest,
				isKanban);

		if (isSourceKanban(kpiRequestSourceWiseMap)) {
			return processKanban(kpiID, kpiRequestSourceWiseMap, acceptedFilter);
		}
		return processScrum(kpiID, kpiRequestSourceWiseMap, acceptedFilter);

	}

	/**
	 * Returns true iff source is kanban
	 *
	 * @param kpiRequestSourceWiseMap
	 * @return returns true iff any key of kpiRequestSourceWiseMap is of kanban
	 */
	private boolean isSourceKanban(Map<String, KpiRequest> kpiRequestSourceWiseMap) {
		Set<String> kanbanSources = new HashSet<>();
		kanbanSources.add(EXCEL_JIRAKANBAN);
		kanbanSources.add(EXCEL_ZEPHYRKANBAN);
		kanbanSources.add(EXCEL_SONARKANBAN);
		kanbanSources.add(EXCEL_BIBUCKETKANBAN);
		kanbanSources.add(EXCEL_JENKINSKANBAN);

		return CollectionUtils.containsAny(kpiRequestSourceWiseMap.keySet(), kanbanSources);
	}

	/**
	 * Process request for Scrum Conditional for type of kpi
	 *
	 * @param kpiID
	 * @param kpiRequestSourceWiseMap
	 * @param acceptedFilter
	 * @return Excel data Object
	 */
	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	private Object processScrum(String kpiID, Map<String, KpiRequest> kpiRequestSourceWiseMap,
			List<String> acceptedFilter) {

		List<KpiElement> totalKpiElementList = new ArrayList<>();
		ExecutorService executor = Executors.newFixedThreadPool(10);

		long startTime = System.currentTimeMillis();

		try {
			Future<List<KpiElement>> jiraKpiDataFuture = null;
			Future<List<KpiElement>> sonarKpiDataFuture = null;
			Future<List<KpiElement>> zephyrKpiDataFuture = null;
			Future<List<KpiElement>> bitbucketKpiDataFuture = null;
			Future<List<KpiElement>> jenkinsKpiDataFuture = null;

			for (Map.Entry<String, KpiRequest> pair : kpiRequestSourceWiseMap.entrySet()) {

				cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.EXCEL.name(),
						pair.getValue().getRequestTrackerId());

				switch (pair.getKey()) {
				case EXCEL_JIRA:
					jiraKpiDataFuture = excelJiraKpiDataFuture(executor, pair);
					break;
				case EXCEL_JENKINS:
					jenkinsKpiDataFuture = excelJenkinsKpiDataFuture(executor, pair);
					break;
				case EXCEL_SONAR:
					sonarKpiDataFuture = excelSonarKpiDataFuture(executor, pair);
					break;
				case EXCEL_ZEPHYR:
					zephyrKpiDataFuture = excelZephyrKpiDataFuture(executor, pair);
					break;
				case EXCEL_BITBUCKET:
					bitbucketKpiDataFuture = excelbitBucketKpiDataFuture(executor, pair);
					break;

				default:
					break;
				}
			}

			addAllToKpiElementList(totalKpiElementList, jiraKpiDataFuture);
			addAllToKpiElementList(totalKpiElementList, jenkinsKpiDataFuture);
			addAllToKpiElementList(totalKpiElementList, sonarKpiDataFuture);
			addAllToKpiElementList(totalKpiElementList, zephyrKpiDataFuture);
			addAllToKpiElementList(totalKpiElementList, bitbucketKpiDataFuture);

		} catch (InterruptedException ie) {
			log.error("InterruptedException: ", ie);
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			log.error("[KPI-EXCEL-SERVICE]. Error while getting data from colletor sources services {}", e);
		} finally {
			executor.shutdown();
		}

		long processTime = System.currentTimeMillis() - startTime;
		log.info("[KPI-EXCEL-SERVICE]. Time taken to process Excel kpi data request: {}", processTime);

		if (null != kpiID) {
			return createKpiExcelValidationDataResponse(totalKpiElementList);
		} else {
			log.info("[KPI-EXCEL-SERVICE]. kpiId is Invalid ");
			return null;
		}

	}

	/**
	 * Process request for Kanban. Conditional for types kpi
	 *
	 * @param kpiID
	 * @param kpiRequestSourceWiseMap
	 * @param acceptedFilter
	 * @return Excel data Object
	 */
	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	private Object processKanban(String kpiID, Map<String, KpiRequest> kpiRequestSourceWiseMap,
			List<String> acceptedFilter) {

		List<KpiElement> totalKpiElementList = new ArrayList<>();
		ExecutorService executor = Executors.newFixedThreadPool(10);
		long startTime = System.currentTimeMillis();

		try {
			Future<List<KpiElement>> jiraKanbanKpiDataFuture = null;
			Future<List<KpiElement>> zephyrKanbanKpiDataFuture = null;
			Future<List<KpiElement>> sonarKanbanKpiDataFuture = null;
			Future<List<KpiElement>> bitbucketKanbanKpiDataFuture = null;
			Future<List<KpiElement>> jenkinsKanbanKpiDataFuture = null;

			for (Map.Entry<String, KpiRequest> pair : kpiRequestSourceWiseMap.entrySet()) {

				cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.EXCEL.name(),
						pair.getValue().getRequestTrackerId());

				switch (pair.getKey()) {
				case EXCEL_JIRAKANBAN:
					jiraKanbanKpiDataFuture = excelJiraKanbanKpiDataFuture(executor, pair);
					break;
				case EXCEL_ZEPHYRKANBAN:
					zephyrKanbanKpiDataFuture = excelZephyreKanbanKpiDataFuture(executor, pair);

					break;
				case EXCEL_SONARKANBAN:
					sonarKanbanKpiDataFuture = excelSonarKanbanKpiDataFuture(executor, pair);
					break;
				case EXCEL_BIBUCKETKANBAN:
					bitbucketKanbanKpiDataFuture = excelBitBucketKanbanKpiDataFuture(executor, pair);
					break;
				case EXCEL_JENKINSKANBAN:
					jenkinsKanbanKpiDataFuture = excelJenkinsKanbanKpiDataFuture(executor, pair);
					break;
				default:
					break;
				}
			}

			addAllToKpiElementList(totalKpiElementList, jiraKanbanKpiDataFuture);
			addAllToKpiElementList(totalKpiElementList, zephyrKanbanKpiDataFuture);
			addAllToKpiElementList(totalKpiElementList, sonarKanbanKpiDataFuture);
			addAllToKpiElementList(totalKpiElementList, bitbucketKanbanKpiDataFuture);
			addAllToKpiElementList(totalKpiElementList, jenkinsKanbanKpiDataFuture);

		} catch (InterruptedException ie) {
			log.error("InterruptedException: ", ie);
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			log.error("[KPI-EXCEL-SERVICE]. Error while getting data from colletor sources services {}", e);
		} finally {
			executor.shutdown();
		}

		long processTime = System.currentTimeMillis() - startTime;
		log.info("[KPI-EXCEL-SERVICE]. Time taken to process Excel kpi data request: {}", processTime);

		if (null != kpiID) {
			return createKpiExcelValidationDataResponse(totalKpiElementList);
		} else {
			log.info("[KPI-EXCEL-SERVICE]. kpiId is Invalid ");
			return null;
		}

	}

	/**
	 * Create and return KPIExcelValidationDataResponse
	 *
	 * @param totalKpiElementList
	 * @return Excel validation data response
	 */
	private Object createKpiExcelValidationDataResponse(List<KpiElement> totalKpiElementList) {
		KPIExcelValidationDataResponse kpiExcelValidationDataResponse = new KPIExcelValidationDataResponse();
		prepareKpiExcelValidationDataResponse(kpiExcelValidationDataResponse, totalKpiElementList);
		return kpiExcelValidationDataResponse;
	}

	/**
	 * Gets result from future and sets it into elements
	 *
	 * @param totalKpiElementList
	 * @param jiraKpiDataFuture
	 * @throws InterruptedException
	 * @throws java.util.concurrent.ExecutionException
	 */
	private void addAllToKpiElementList(List<KpiElement> totalKpiElementList,
			Future<List<KpiElement>> jiraKpiDataFuture)
			throws InterruptedException, java.util.concurrent.ExecutionException {
		if (null != jiraKpiDataFuture) {
			totalKpiElementList.addAll(jiraKpiDataFuture.get());
		}
	}

	/**
	 * Submits job to the executor and provides future object
	 *
	 * @param executor
	 * @param pair
	 * @return
	 */
	private Future<List<KpiElement>> excelJenkinsKanbanKpiDataFuture(ExecutorService executor,
			Map.Entry<String, KpiRequest> pair) {
		Future<List<KpiElement>> jenkinsKanbanKpiDataFuture;
		cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JENKINSKANBAN.name(),
				pair.getValue().getRequestTrackerId());

		Callable<List<KpiElement>> jenkinsKanbanKpiDataTask = () -> jenkinsServiceKanbanR.process(pair.getValue());

		jenkinsKanbanKpiDataFuture = executor.submit(jenkinsKanbanKpiDataTask);
		return jenkinsKanbanKpiDataFuture;
	}

	/**
	 * Submits job to the executor and provides future object
	 *
	 * @param executor
	 * @param pair
	 * @return
	 */
	private Future<List<KpiElement>> excelBitBucketKanbanKpiDataFuture(ExecutorService executor,
			Map.Entry<String, KpiRequest> pair) {
		Future<List<KpiElement>> bitbucketKanbanKpiDataFuture;
		cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.BITBUCKETKANBAN.name(),
				pair.getValue().getRequestTrackerId());

		Callable<List<KpiElement>> bitbucketKanbanKpiDataTask = () -> bitBucketServiceKanbanR.process(pair.getValue());

		bitbucketKanbanKpiDataFuture = executor.submit(bitbucketKanbanKpiDataTask);
		return bitbucketKanbanKpiDataFuture;
	}

	/**
	 * Submits job to the executor and provides future object
	 *
	 * @param executor
	 * @param pair
	 * @return
	 */
	private Future<List<KpiElement>> excelSonarKanbanKpiDataFuture(ExecutorService executor,
			Map.Entry<String, KpiRequest> pair) {
		Future<List<KpiElement>> sonarKanbanKpiDataFuture;
		cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.SONARKANBAN.name(),
				pair.getValue().getRequestTrackerId());

		Callable<List<KpiElement>> sonarKanbanKpiDataTask = () -> sonarServiceKanbanR.process(pair.getValue());

		sonarKanbanKpiDataFuture = executor.submit(sonarKanbanKpiDataTask);
		return sonarKanbanKpiDataFuture;
	}

	/**
	 * Submits job to the executor and provides future object
	 *
	 * @param executor
	 * @param pair
	 * @return
	 */
	private Future<List<KpiElement>> excelZephyreKanbanKpiDataFuture(ExecutorService executor,
			Map.Entry<String, KpiRequest> pair) {
		Future<List<KpiElement>> zephyrKanbanKpiDataFuture;
		cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.ZEPHYRKANBAN.name(),
				pair.getValue().getRequestTrackerId());

		Callable<List<KpiElement>> zephyrKanbanKpiDataTask = () -> zephyrServiceKanban.process(pair.getValue());

		zephyrKanbanKpiDataFuture = executor.submit(zephyrKanbanKpiDataTask);
		return zephyrKanbanKpiDataFuture;
	}

	/**
	 * Submits job to the executor and provides future object
	 *
	 * @param executor
	 * @param pair
	 * @return
	 */
	private Future<List<KpiElement>> excelJiraKanbanKpiDataFuture(ExecutorService executor,
			Map.Entry<String, KpiRequest> pair) {
		Future<List<KpiElement>> jiraKanbanKpiDataFuture;
		cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRAKANBAN.name(),
				pair.getValue().getRequestTrackerId());

		Callable<List<KpiElement>> jiraKanbanKpiDataTask = () -> jiraServiceKanbanR.process(pair.getValue());

		jiraKanbanKpiDataFuture = executor.submit(jiraKanbanKpiDataTask);
		return jiraKanbanKpiDataFuture;
	}

	/**
	 * Submits job to the executor and provides future object
	 *
	 * @param executor
	 * @param pair
	 * @return
	 */
	private Future<List<KpiElement>> excelbitBucketKpiDataFuture(ExecutorService executor,
			Map.Entry<String, KpiRequest> pair) {
		Future<List<KpiElement>> bitbucketKpiDataFuture;
		cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.BITBUCKET.name(),
				pair.getValue().getRequestTrackerId());

		Callable<List<KpiElement>> bitbucketKpiDataTask = () -> bitBucketServiceR.process(pair.getValue());

		bitbucketKpiDataFuture = executor.submit(bitbucketKpiDataTask);
		return bitbucketKpiDataFuture;
	}

	/**
	 * Submits job to the executor and provides future object
	 *
	 * @param executor
	 * @param pair
	 * @return
	 */
	private Future<List<KpiElement>> excelZephyrKpiDataFuture(ExecutorService executor,
			Map.Entry<String, KpiRequest> pair) {
		Future<List<KpiElement>> zephyrKpiDataFuture;
		cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.ZEPHYR.name(),
				pair.getValue().getRequestTrackerId());

		Callable<List<KpiElement>> zephyrKpiDataTask = () -> zephyrService.process(pair.getValue());

		zephyrKpiDataFuture = executor.submit(zephyrKpiDataTask);
		return zephyrKpiDataFuture;
	}

	/**
	 * Submits job to the executor and provides future object
	 *
	 * @param executor
	 * @param pair
	 * @return
	 */
	private Future<List<KpiElement>> excelSonarKpiDataFuture(ExecutorService executor,
			Map.Entry<String, KpiRequest> pair) {
		Future<List<KpiElement>> sonarKpiDataFuture;
		cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.SONAR.name(),
				pair.getValue().getRequestTrackerId());

		Callable<List<KpiElement>> sonarKpiDataTask = () -> sonarServiceR.process(pair.getValue());

		sonarKpiDataFuture = executor.submit(sonarKpiDataTask);
		return sonarKpiDataFuture;
	}

	/**
	 * Submits job to the executor and provides future object
	 *
	 * @param executor
	 * @param pair
	 * @return
	 */
	private Future<List<KpiElement>> excelJenkinsKpiDataFuture(ExecutorService executor,
			Map.Entry<String, KpiRequest> pair) {
		Future<List<KpiElement>> jenkinsKpiDataFuture;
		cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JENKINS.name(),
				pair.getValue().getRequestTrackerId());

		Callable<List<KpiElement>> jenkinsKpiDataTask = () -> jenkinsServiceR.process(pair.getValue());

		jenkinsKpiDataFuture = executor.submit(jenkinsKpiDataTask);
		return jenkinsKpiDataFuture;
	}

	/**
	 * Submits job to the executor and provides future object
	 *
	 * @param executor
	 * @param pair
	 * @return
	 */
	private Future<List<KpiElement>> excelJiraKpiDataFuture(ExecutorService executor,
			Map.Entry<String, KpiRequest> pair) {
		Future<List<KpiElement>> jiraKpiDataFuture;
		cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name(),
				pair.getValue().getRequestTrackerId());

		Callable<List<KpiElement>> jiraKpiDataTask = () -> jiraServiceR.process(pair.getValue());

		jiraKpiDataFuture = executor.submit(jiraKpiDataTask);
		return jiraKpiDataFuture;
	}

	/**
	 * Sets values kpi details in validation data response
	 *
	 * @param kpiExcelValidationDataResponse
	 * @param totalKpiElementList
	 */
	private void prepareKpiExcelValidationDataResponse(KPIExcelValidationDataResponse kpiExcelValidationDataResponse,
			List<KpiElement> totalKpiElementList) {

		// totalKpiElementList will have only one KPI data at any given time
		totalKpiElementList.forEach(element -> {

			kpiExcelValidationDataResponse.setKpiId(element.getKpiId());
			kpiExcelValidationDataResponse.setKpiName(element.getKpiName());

			Map<String, ValidationData> mapOfSprintAndData = element.getMapOfSprintAndData();
			kpiExcelValidationDataResponse.setMapOfSprintAndData(mapOfSprintAndData);
			kpiExcelValidationDataResponse.setExcelData(element.getExcelData());
			kpiExcelValidationDataResponse.setExcelColumns(element.getExcelColumns());
		});

	}

	/**
	 * Creates KPI request for each sources.
	 *
	 * @param kpiID
	 *            the kpi id
	 * @param level
	 *            same as level received in the KPI request
	 * @param filterIds
	 *            same as ids received in KPI API request
	 * @param kpiReq
	 *            the kpi req
	 * @param isKanban
	 *            the is kanban
	 * @return map of source with kpi request.
	 */
	public Map<String, KpiRequest> createKPIRequest(String kpiID, int level, List<String> filterIds, KpiRequest kpiReq,
			Boolean isKanban) {

		List<String> kpiSourceList = null;

		// Get all Collector source tools
		kpiSourceList = Stream.of(KPICode.values()).map(KPICode::getSource).distinct()
				.filter(source -> !source.equalsIgnoreCase(KPICode.INVALID.name())).collect(Collectors.toList());

		if (null == kpiID) {
			if (null != isKanban) {
				if (Boolean.TRUE.equals(isKanban)) {
					kpiSourceList = kpiSourceList.stream().filter(source -> source.contains(KANBAN))
							.collect(Collectors.toList());
				} else {
					kpiSourceList = kpiSourceList.stream().filter(source -> !source.contains(KANBAN))
							.collect(Collectors.toList());
				}
			}
		} else {
			kpiSourceList = kpiSourceList.stream()
					.filter(source -> source.equalsIgnoreCase(KPICode.getKPI(kpiID).getSource()))
					.collect(Collectors.toList());
		}

		// Prepare List of KPI Request
		Map<String, KpiRequest> kpiRequestSourceWiseMap = new HashMap<>();
		kpiSourceList.forEach(source -> {

			KpiRequest kpiRequest = kpiReq;
			if (null == kpiRequest) {
				kpiRequest = new KpiRequest();
			}

			List<KpiElement> kpiElementList = new ArrayList<>();
			List<KpiMaster> masterList = (List<KpiMaster>) configHelperService.loadKpiMaster();
			if (null == kpiID) {
				List<String> masterKpiIdList = masterList.stream().map(KpiMaster::getKpiId)
						.collect(Collectors.toList());
				Stream.of(KPICode.values()).filter(kpi -> kpi != KPICode.INVALID
						&& kpi.getSource().equalsIgnoreCase(source) && masterKpiIdList.contains(kpi.getKpiId()))
						.forEach(kpi -> {

							KpiElement kpiElement = new KpiElement();
							kpiElement.setKpiId(kpi.getKpiId());
							kpiElement.setKpiName(kpi.name());
							kpiElement.setKpiSource(KPISource.EXCEL.name() + "-" + source);
							kpiElement.setKpiCategory(masterList.stream()
									.filter(kpiMaster -> kpiMaster.getKpiId().equalsIgnoreCase(kpi.getKpiId()))
									.map(KpiMaster::getKpiCategory).filter(StringUtils::isNotEmpty).findFirst()
									.orElse(""));

							kpiElementList.add(kpiElement);
						});
			} else {

				KPICode kpi = KPICode.getKPI(kpiID);

				KpiElement kpiElement = new KpiElement();
				kpiElement.setKpiId(kpi.getKpiId());
				kpiElement.setKpiName(kpi.name());
				kpiElement.setKpiSource(KPISource.EXCEL.name() + "-" + source);
				kpiElement.setKpiCategory(
						masterList.stream().filter(kpiMaster -> kpiMaster.getKpiId().equalsIgnoreCase(kpi.getKpiId()))
								.map(KpiMaster::getKpiCategory).filter(StringUtils::isNotEmpty).findFirst().orElse(""));

				kpiElementList.add(kpiElement);

			}

			kpiRequest.setLevel(level);
			kpiRequest.setIds(filterIds.parallelStream().toArray(String[]::new));
			kpiRequest.setKpiList(kpiElementList);

			kpiRequestSourceWiseMap.put(kpiRequest.getKpiList().get(0).getKpiSource(), kpiRequest);
		});

		return kpiRequestSourceWiseMap;

	}

}
