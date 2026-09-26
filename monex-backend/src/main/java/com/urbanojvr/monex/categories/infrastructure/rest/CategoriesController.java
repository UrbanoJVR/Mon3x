package com.urbanojvr.monex.categories.infrastructure.rest;

import com.urbanojvr.monex.categories.CategoriesApi;
import com.urbanojvr.monex.categories.model.CreateCategoryRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoriesController implements CategoriesApi {

	@Override
	public ResponseEntity<Void> createCategory(CreateCategoryRequest createCategoryRequest) {
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
}
