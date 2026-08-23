package com.trucdecomptable.cuissonvapeur.ui.screens.catalogue

import androidx.lifecycle.ViewModel
import com.trucdecomptable.cuissonvapeur.data.repository.VegetableRepository
import com.trucdecomptable.cuissonvapeur.domain.model.Vegetable
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VegetableDetailViewModel @Inject constructor(
    private val vegetableRepository: VegetableRepository,
) : ViewModel() {
    fun findVegetable(vegetableId: String): Vegetable? = vegetableRepository.findById(vegetableId)
}
