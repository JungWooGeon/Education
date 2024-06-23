package com.pass.presentation.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pass.domain.model.LiveStreaming
import com.pass.domain.usecase.GetLiveStreamingListUseCase
import com.pass.domain.usecase.IsSignedInUseCase
import com.pass.domain.util.URLCodec
import com.pass.presentation.intent.LiveListIntent
import com.pass.presentation.sideeffect.LiveListSideEffect
import com.pass.presentation.state.screen.LiveListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import org.webrtc.VideoTrack
import javax.inject.Inject

@HiltViewModel
class LiveListViewModel @Inject constructor(
    private val isSignedInUseCase: IsSignedInUseCase,
    private val getLiveStreamingListUseCase: GetLiveStreamingListUseCase<VideoTrack, Bitmap>,
    private val urlCodec: URLCodec<Uri>
) : ViewModel(), ContainerHost<LiveListState, LiveListSideEffect> {

    override val container: Container<LiveListState, LiveListSideEffect> = container(
        initialState = LiveListState()
    )

    fun processIntent(intent: LiveListIntent) {
        when(intent) {
            is LiveListIntent.GetLiveList -> getLiveList()
            is LiveListIntent.StartLiveStreamingActivity -> startLiveStreamingActivity()
            is LiveListIntent.OnClickLiveStreamingItem -> onClickLiveStreamingItem(intent.broadcastId)
        }
    }

    private fun getLiveList() = intent {
        viewModelScope.launch {
            // 라이브 리스트 정보 확인
            val result = getLiveStreamingListUseCase().first()
            result.onSuccess { liveStreamingList ->
                val mutableLiveList = mutableListOf<LiveStreaming>()

                liveStreamingList.forEach {
                    mutableLiveList.add(
                        LiveStreaming(
                            broadcastId = it.broadcastId,
                            thumbnailURL = urlCodec.urlDecode(it.thumbnailURL),
                            title = it.title,
                            userProfileURL = it.userProfileURL,
                            userName = it.userName
                        )
                    )
                }

                reduce {
                    state.copy(liveStreamingList = mutableLiveList.toList())
                }
            }.onFailure {
                postSideEffect(LiveListSideEffect.Toast(it.message ?: "라이브 중인 방송이 없습니다."))
            }
        }

        viewModelScope.launch {
            // 로그인 정보 확인
            val result = isSignedInUseCase().first()
            reduce {
                state.copy(isLoggedIn = result)
            }
        }
    }

    private fun startLiveStreamingActivity() = intent {
        if (state.isLoggedIn) {
            postSideEffect(LiveListSideEffect.StartLiveStreamingActivity)
        } else {
            postSideEffect(LiveListSideEffect.Toast("로그인이 필요합니다."))
            postSideEffect(LiveListSideEffect.NavigateLogInScreen)
        }
    }

    private fun onClickLiveStreamingItem(broadcastId: String) = intent {
        if (state.isLoggedIn) {
            postSideEffect(LiveListSideEffect.StartWatchBroadCastActivity(broadcastId))
        } else {
            postSideEffect(LiveListSideEffect.Toast("로그인이 필요합니다."))
            postSideEffect(LiveListSideEffect.NavigateLogInScreen)
        }
    }
}