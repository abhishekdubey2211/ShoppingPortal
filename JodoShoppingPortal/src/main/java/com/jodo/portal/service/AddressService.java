package com.jodo.portal.service;

import com.jodo.portal.dto.RessidentialAddressDTO;
import com.jodo.portal.model.EndUser;

public interface AddressService {
    RessidentialAddressDTO addAddress(EndUser user, RessidentialAddressDTO pushAddressDTO);
}
