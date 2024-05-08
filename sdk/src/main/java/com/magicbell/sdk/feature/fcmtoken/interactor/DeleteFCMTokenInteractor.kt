package com.magicbell.sdk.feature.fcmtoken.interactor

import com.mobilejazz.harmony.domain.interactor.DeleteInteractor
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.fcmtoken.data.DeleteFCMTokenQuery
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class DeleteFCMTokenInteractor(
  private val coroutineContext: CoroutineContext,
  private val deleteFCMTokenInteractor: DeleteInteractor,
) {

  suspend operator fun invoke(deviceToken: String, userQuery: UserQuery) {
    return withContext(coroutineContext) {
      deleteFCMTokenInteractor(DeleteFCMTokenQuery(deviceToken, userQuery))
    }
  }
}