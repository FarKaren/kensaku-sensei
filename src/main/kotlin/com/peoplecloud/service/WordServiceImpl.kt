package com.peoplecloud.service

import com.peoplecloud.client.dictionary.YandexDictionaryClient
import com.peoplecloud.config.Env
import com.peoplecloud.dto.word.*
import com.peoplecloud.exceptions.EntityNotFoundException
import com.peoplecloud.models.DefinitionModel
import com.peoplecloud.models.Languages
import com.peoplecloud.models.SynonymModel
import com.peoplecloud.models.TranslationModel
import com.peoplecloud.repository.DefinitionRepository
import com.peoplecloud.repository.EnglishRepository
import com.peoplecloud.repository.PortugueseRepository
import com.peoplecloud.repository.RussianRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WordServiceImpl(
    private val definitionRepository: DefinitionRepository,
    private val dictionaryClient: YandexDictionaryClient,
    private val englishRepository: EnglishRepository,
    private val portugueseRepository: PortugueseRepository,
    private val russianRepository: RussianRepository
) : WordService {
    companion object {
        val log: Logger = LoggerFactory.getLogger(WordServiceImpl::class.java)
    }

    private val yandexDictionaryApiKey: String = Env.get("YANDEX_DICTIONARY_API_KEY")
        ?: throw IllegalArgumentException("YANDEX_DICTIONARY_API_KEY is not defined")

    @Transactional
    override fun getWordCard(wordId: Long, language: Languages): YandexResponse {
        log.info("method getWordCard() invoked")
        val lang =
            when (language) {
                Languages.Portuguese -> "pt-pt"
                Languages.English -> "en-en"
                Languages.Russian -> "ru-ru"
            }
        val word =
            when (language) {
                Languages.Portuguese -> portugueseRepository.findById(wordId)
                    .orElseThrow { EntityNotFoundException("Word with id: $wordId not found") }.portuguese!!

                Languages.Russian -> russianRepository.findById(wordId)
                    .orElseThrow { EntityNotFoundException("Word with id: $wordId not found") }.russian!!

                Languages.English -> englishRepository.findById(wordId)
                    .orElseThrow { EntityNotFoundException("Word with id: $wordId not found") }.english!!
            }
        val definitions = definitionRepository.findByWordIdAndLanguage(wordId, language)
        val yandexResponse = YandexResponse(
            def = definitions.map { definition ->
                Definition(
                    text = definition.text!!,
                    pos = definition.pos,
                    tr = definition.translations.map { translation ->
                        Translation(
                            text = translation.text!!,
                            pos = translation.pos,
                            syn = translation.synonyms.map { synonym ->
                                Synonym(text = synonym.text!!)
                            }
                        )
                    }
                )
            }
        )

        return if (definitions.isEmpty()) {
            var clientResponse = YandexResponse()
            try {
                clientResponse = dictionaryClient.lookup(
                    yandexDictionaryApiKey,
                    lang,
                    word
                )
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
            clientResponse.def.forEach { definitionDto ->
                val definition = DefinitionModel().apply {
                    text = definitionDto.text
                    pos = definitionDto.pos
                    this.wordId = wordId
                    this.language = language
                }

                val translations = definitionDto.tr.map { translationDto ->
                    val translation = TranslationModel().apply {
                        text = translationDto.text
                        pos = translationDto.pos
                        this.definition = definition // Ссылка на родительскую сущность
                    }

                    val synonyms = translationDto.syn.map { synonymDto ->
                        SynonymModel().apply {
                            text = synonymDto.text
                            this.translation = translation // Ссылка на родительскую сущность
                        }
                    }.toMutableList()

                    // Назначаем список синонимов
                    translation.synonyms = synonyms
                    translation
                }.toMutableList()

                // Назначаем список переводов созданной сущности определения
                definition.translations = translations

                // Сохраняем определение в репозиторий, что автоматически сохранит и переводы, и синонимы (благодаря каскаду)
                definitionRepository.save(definition)
            }
            clientResponse
        } else yandexResponse

    }
}