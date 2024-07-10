package com.peoplecloud.service.translate

import com.deepl.api.Translator
import com.peoplecloud.dto.processor.PicDataDto
import com.peoplecloud.exceptions.EntityNotFoundException
import com.peoplecloud.exceptions.UnsupportedLanguageException
import com.peoplecloud.models.toPicDataDto
import com.peoplecloud.repository.DeeplLangRepository
import com.peoplecloud.repository.EnglishRepository
import com.peoplecloud.repository.PortugueseRepository
import com.peoplecloud.repository.RussianRepository
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class TranslateServiceImpl(
    private val translator: Translator,
    private val deeplLangsRepository: DeeplLangRepository,
    private val englishRepository: EnglishRepository,
    private val russianRepository: RussianRepository,
    private val portugueseRepository: PortugueseRepository
) : TranslateService {

    companion object {
        val log: Logger = LoggerFactory.getLogger(TranslateServiceImpl::class.java)
        private const val DEEPL_SRC_LANG = "ja"
    }

    @Transactional
    override fun translateAndGetPicData(input: String, tgtLang: String): TranslateService.TranslateRs {
        log.info("method translate() invoked")
        val lang = deeplLangsRepository.findByLanguage(tgtLang)
            ?: throw EntityNotFoundException("Language with code: $tgtLang not found")
        if (!lang.isSupported!!)
            throw UnsupportedLanguageException("Language '$tgtLang' is not supported")

        val setInputWords = input.split(";").toSet()
        val recordsFromDb = getRecordsFromDatabase(setInputWords, tgtLang)
        val newWords = filterExistedWords(setInputWords, recordsFromDb)

        val newPicData =
            if (newWords.isNotEmpty()) {
                val translatedWords = translator.translateText(newWords, DEEPL_SRC_LANG, lang.code).text
                zipInputAndTranslatedWords(newWords, translatedWords)
            } else emptyList()

        return TranslateService.TranslateRs(recordsFromDb, newPicData, newWords)
    }

    private fun getRecordsFromDatabase(inputWords: Set<String>, tgtLang: String): List<PicDataDto> {
        return when (tgtLang) {
            "English" -> englishRepository.findByJapaneseIn(inputWords)
                .map { it.toPicDataDto() }

            "Russian" -> russianRepository.findByJapaneseIn(inputWords)
                .map { it.toPicDataDto() }

            else -> portugueseRepository.findByJapaneseIn(inputWords)
                .map { it.toPicDataDto() }
        }
    }

    private fun filterExistedWords(inputWords: Set<String>, recordsFromDb: List<PicDataDto>): String {
        val existedJapaneseWords = recordsFromDb.map { it.sourceWord }
        val existedWords = inputWords.filter { !existedJapaneseWords.contains(it) }
        return existedWords.stream().collect(Collectors.joining(";"))
    }

    private fun zipInputAndTranslatedWords(input: String, translate: String): List<PicDataDto> {
        val inputSplit = input.split(";").map { it.trim().replace(Regex("[!@#\$%^&*?>:.]"), "") }
        val resultSplit = translate.split(";").map { it.trim().replace(Regex("[!@#\$%^&*?>:.]"), "") }

        val maxLength = maxOf(inputSplit.size, resultSplit.size)
        return List(maxLength) { index ->
            val first = inputSplit.getOrElse(index) { "" }
            val second = resultSplit.getOrElse(index) { "" }
            PicDataDto(sourceWord =  first, targetWord =  second)
        }
    }
}