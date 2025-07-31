package beh59.aber.ac.uk.cs39440.mmp.domain.friend

import beh59.aber.ac.uk.cs39440.mmp.data.repository.IFriendRepository
import javax.inject.Inject

/**
 * GetFriendsUseCase
 * Encapsulates and coordinates logic related to retrieving friend data
 * @param friendsRepository Contains business logic and Firestore calls related to friend
 * systems in the application
 */
class GetFriendsUseCase @Inject constructor(
    private val friendsRepository: IFriendRepository
) {
    /**
     * invoke
     * Called when GetFriendsUseCase is called. Calls FriendRepository for retrieval of friends
     * @param currentUserUID The unique identifier of the current user
     */
    suspend operator fun invoke(currentUserUID: String) {
        return friendsRepository.getFriends(currentUserUID)
    }
}