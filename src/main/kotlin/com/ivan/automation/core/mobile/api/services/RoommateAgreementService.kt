package com.ivan.automation.core.mobile.api.services

import com.ivan.automation.core.mobile.api.ServiceConstants.DASHBOARD_URI
import com.ivan.automation.core.mobile.api.models.response.ContractData
import com.ivan.automation.core.mobile.api.models.response.DataResponse
import com.ivan.automation.core.mobile.api.models.response.DistributorResponse
import com.ivan.automation.core.mobile.api.wrapper.RequestWrapper
import io.qameta.allure.Step
import io.restassured.http.ContentType
import org.apache.http.HttpStatus
import org.hamcrest.Matchers.equalTo

@Suppress("TooManyFunctions", "UNCHECKED_CAST")
object RoommateAgreementService {

    ////////////////////////////////////////////////////////////////////////////////
    // Distributor Operations
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new distributor.
     *
     * In case if the distributor already exists,
     * an HTTP 403 with the message "Channel already exists." and code "ALREADY_EXIST" is handled gracefully,
     * returning null.
     *
     * @param distributorName The name of the distributor to create.
     * @return A [DataResponse] instance containing the created distributor data.
     */
    @Step("POST - /roommate-agreement/api/v1/distributors")
    fun createDistributor(distributorName: String): DataResponse? {
        val rawResponse = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .deviceIdHeader()
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .body(mapOf("name" to distributorName))
            .post("/roommate-agreement/api/v1/distributors")
            .getResponse()

        return when (rawResponse.statusCode) {
            HttpStatus.SC_CREATED -> {
                rawResponse.`as`(DataResponse::class.java)
            }

            HttpStatus.SC_FORBIDDEN -> {
                val jsonPath = rawResponse.jsonPath()
                val message = jsonPath.getString("message")
                val code = jsonPath.getString("code")
                if (message == "Distributor already exists." && code.contains("ALREADY_EXIST")) {
                    null
                } else {
                    throw AssertionError("Unexpected error: message = $message, code = $code")
                }
            }

            else -> throw AssertionError("Unexpected status code: ${rawResponse.statusCode}")
        }
    }

    /**
     * Retrieves the list of existing distributors.
     *
     * @return A list of [DistributorResponse] objects representing the distributors.
     */
    @Step("GET - /roommate-agreement/api/v1/distributors")
    fun getDistributors(): List<DistributorResponse> {
        val response = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .deviceIdHeader()
            .dashBoardAuthHeader()
            .get("/roommate-agreement/api/v1/distributors")
            .status(equalTo(HttpStatus.SC_OK))
        val distributorsArray =
            response.getBody().`as`(Array<DistributorResponse>::class.java) as? Array<DistributorResponse>
                ?: emptyArray()
        return distributorsArray.toList()
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Cost Category Operations
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new cost category for a distributor.
     *
     * @param distributorId The unique identifier of the distributor.
     * @param request The request payload for creating the cost category.
     * @return A [DataResponse] instance containing the created cost category data.
     */
    @Step("POST - /roommate-agreement/api/v1/distributors/{distributorId}/costcategories")
    fun createCostCategory(distributorId: String, request: CreateCostCategoryRequest): DataResponse {
        val response = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .deviceIdHeader()
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .pathParam("distributorId", distributorId)
            .body(request)
            .post("/roommate-agreement/api/v1/distributors/{distributorId}/costcategories")
            .status(equalTo(HttpStatus.SC_CREATED))
        return response.getResponse().`as`(DataResponse::class.java)
    }

    /**
     * Updates the draft of an existing cost category for a distributor.
     *
     * @param distributorId The unique identifier of the distributor.
     * @param costCategoryId The unique identifier of the cost category.
     * @param request The request payload with the updated cost category details.
     */
    @Step("PUT - /roommate-agreement/api/v1/distributors/{distributorId}/costcategories/{costCategoryId}/draft")
    fun updateDraftCostCategory(
        distributorId: String,
        costCategoryId: String,
        request: CreateCostCategoryRequest
    ) {
        RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .deviceIdHeader()
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .pathParam("distributorId", distributorId)
            .pathParam("costCategoryId", costCategoryId)
            .body(request)
            .put("/roommate-agreement/api/v1/distributors/{distributorId}/costcategories/{costCategoryId}/draft")
            .status(equalTo(HttpStatus.SC_NO_CONTENT))
    }

    /**
     * Approves a cost category for a distributor.
     *
     * @param distributorId The unique identifier of the distributor.
     * @param costCategoryId The unique identifier of the cost category.
     * @param request The request payload for approving the cost category.
     * @return A [DataResponse] containing the approved cost category data.
     */
    @Step("POST - /roommate-agreement/api/v1/distributors/{distributorId}/costcategories/{costCategoryId}")
    fun approveCostCategory(
        distributorId: String,
        costCategoryId: String,
        request: CreateCostCategoryRequest
    ): DataResponse {
        val response = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .deviceIdHeader()
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .pathParam("distributorId", distributorId)
            .pathParam("costCategoryId", costCategoryId)
            .body(request)
            .post("/roommate-agreement/api/v1/distributors/{distributorId}/costcategories/{costCategoryId}")
            .status(equalTo(HttpStatus.SC_CREATED))
        return response.getResponse().`as`(DataResponse::class.java)
    }

    /**
     * Removes a cost category for a distributor.
     *
     * @param distributorId The unique identifier of the distributor.
     * @param costCategoryId The unique identifier of the cost category to delete.
     */
    @Step("DELETE - /roommate-agreement/api/v1/distributors/{distributorId}/costcategories/{costCategoryId}")
    fun removeCostCategory(distributorId: String, costCategoryId: String) {
        RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .deviceIdHeader()
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .pathParam("distributorId", distributorId)
            .pathParam("costCategoryId", costCategoryId)
            .delete("/roommate-agreement/api/v1/distributors/{distributorId}/costcategories/{costCategoryId}")
            .status(equalTo(HttpStatus.SC_NO_CONTENT))
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Contracts Operations
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new contract for a distributor.
     *
     * @param distributorId The unique identifier of the distributor.
     * @param request The request payload for creating the contract.
     * @return A [DataResponse] instance containing the created contract data.
     */
    @Step("POST - /roommate-agreement/api/v1/distributors/{distributorId}/contracts")
    fun createContract(distributorId: String, request: CreateContractRequest): DataResponse? {
        val rawResponse = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .deviceIdHeader()
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .pathParam("distributorId", distributorId)
            .body(request)
            .post("/roommate-agreement/api/v1/distributors/{distributorId}/contracts")
            .getResponse()

        return when (rawResponse.statusCode) {
            HttpStatus.SC_CREATED -> {
                rawResponse.`as`(DataResponse::class.java)
            }

            HttpStatus.SC_FORBIDDEN -> {
                val jsonPath = rawResponse.jsonPath()
                val message = jsonPath.getString("message")
                val code = jsonPath.getString("code")
                if (code.contains("ALREADY_EXIST")) {
                    null
                } else {
                    throw AssertionError("Unexpected error: message = $message, code = $code")
                }
            }

            else -> throw AssertionError("Unexpected status code: ${rawResponse.statusCode}")
        }
    }

    /**
     * Retrieves the data of specific contract.
     *
     * @return A [ContractData] objects representing the contract.
     */
    @Step("GET - /roommate-agreement/api/v1/distributors/{distributorId}/contracts/{contractId}")
    fun getContract(distributorId: String, contractId: String): ContractData {
        val response = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .deviceIdHeader()
            .dashBoardAuthHeader()
            .pathParam("distributorId", distributorId)
            .pathParam("contractId", contractId)
            .get("/roommate-agreement/api/v1/distributors/{distributorId}/contracts/{contractId}")
            .status(equalTo(HttpStatus.SC_OK))
        return response.getResponse().`as`(ContractData::class.java)
    }

    /**
     * Retrieves the list of existing contracts.
     *
     * @param distributorId The unique identifier of the distributor.
     *
     * @return A list of [ContractData] objects representing the contracts.
     */
    @Step("GET - /roommate-agreement/api/v1/distributors/{distributorId}/contracts")
    fun getContracts(distributorId: String): List<ContractData> {
        val response = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .deviceIdHeader()
            .dashBoardAuthHeader()
            .pathParam("distributorId", distributorId)
            .get("/roommate-agreement/api/v1/distributors/{distributorId}/contracts")
            .status(equalTo(HttpStatus.SC_OK))
        val contractResponses =
            response.getBody().`as`(Array<ContractData>::class.java) as? Array<ContractData>
                ?: emptyArray()
        return contractResponses.toList()
    }

    /**
     * Removes a contract.
     *
     * @param distributorId The unique identifier of the distributor to delete.
     * @param contractId The unique identifier of the contract to delete.
     */
    @Step("DELETE - /roommate-agreement/api/v1/distributors/{distributorId}/contracts/{contractId}")
    fun deleteContract(distributorId: String, contractId: String) {
        RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .deviceIdHeader()
            .dashBoardAuthHeader()
            .pathParam("distributorId", distributorId)
            .pathParam("contractId", contractId)
            .delete("/roommate-agreement/api/v1/distributors/{distributorId}/contracts/{contractId}")
            .status(equalTo(HttpStatus.SC_NO_CONTENT))
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Saleability Windows Operations
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a relation between a cost category and content via saleability windows.
     *
     * @param request The request payload containing saleability windows details.
     * @return A [DataResponse] containing the newly created saleability windows data.
     */
    @Step("POST - /roommate-agreement/api/v1/saleabilitywindows")
    fun createSaleabilityWindows(request: CreateSaleabilityWindowsRequest): DataResponse {
        val response = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .deviceIdHeader()
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .body(request)
            .post("/roommate-agreement/api/v1/saleabilitywindows")
            .status(equalTo(HttpStatus.SC_CREATED))
        return response.getResponse().`as`(DataResponse::class.java)
    }

    /**
     * Removes a Saleability Window.
     *
     * @param saleabilityWindowId The unique identifier of the SaleabilityWindow to delete.
     */
    @Step("DELETE - /roommate-agreement/api/v1/distributors/{saleabilityWindowId}")
    fun removeSaleabilityWindow(saleabilityWindowId: String) {
        RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .deviceIdHeader()
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .pathParam("saleabilityWindowId", saleabilityWindowId)
            .delete("/roommate-agreement/api/v1/saleabilitywindows/{saleabilityWindowId}")
            .status(equalTo(HttpStatus.SC_NO_CONTENT))
    }
}
