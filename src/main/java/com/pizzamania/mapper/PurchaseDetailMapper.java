package com.pizzamania.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.pizzamania.dto.PurchaseDetailDto;
import com.pizzamania.model.PurchaseDetail;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PurchaseDetailMapper {

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	PurchaseDetailDto purchaseDetailEntityToDto(PurchaseDetail entity);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	PurchaseDetail purchaseDetailDtoToEntity(PurchaseDetailDto dto);

}
