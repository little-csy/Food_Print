import { Text, View, ScrollView, TouchableOpacity, Image, Modal, SafeAreaView, StyleSheet } from 'react-native';
import React from 'react';
import type { CommunityChallenge } from '../types';
import { useCommunityStyles } from '../hooks';

interface ChallengeDetailModalProps {
  visible: boolean;
  challenge: CommunityChallenge | null;
  onClose: () => void;
  onJoinPress: (id: string) => void;
  onCheckinPress: (id: string) => void;
  onAbandonPress: (id: string) => void;
}

export function ChallengeDetailModal({
  visible,
  challenge,
  onClose,
  onJoinPress,
  onCheckinPress,
  onAbandonPress,
}: ChallengeDetailModalProps) {
  const styles = useCommunityStyles();
  const hasActiveRun = challenge?.joined && !!challenge.userChallengeId;

  if (!challenge) return null;

  const getChallengeCriteriaText = (type?: string, targetValue?: number) => {
    switch (type) {
      case 'CALORIE_CONTROL':
        return 'Keep daily calories within 80%-110% of your target';
      case 'PROTEIN_CHAMPION':
        return `Achieve at least ${targetValue}g of protein per day`;
      case 'LOG_STREAK':
        return 'Log your meals at least once every day';
      case 'LOW_CARB':
        return `Keep daily carbs at or below ${targetValue}g`;
      case 'LIGHT_EATER':
        return `Keep daily calories at or below ${targetValue} kcal`;
      default:
        return '';
    }
  };

  return (
    <Modal visible={visible} animationType="slide" transparent={false} onRequestClose={onClose}>
      <SafeAreaView style={{ flex: 1, backgroundColor: styles.container.backgroundColor }}>
        <ScrollView style={[styles.container, { paddingTop: 0 }]}>
          <View style={[styles.challengeHeaderCard, { backgroundColor: challenge.color }]}>
            <View style={styles.headerContent}>
              <Text style={styles.headerIcon}>{challenge.icon}</Text>
              <View style={styles.headerInfo}>
                <Text style={styles.headerTitle}>{challenge.title}</Text>
                <View style={styles.headerMeta}>
                  <Text style={styles.headerMetaText}>📅 {challenge.duration}</Text>
                  <Text style={styles.headerMetaText}>
                    {hasActiveRun
                      ? `🎯 ${challenge.completedDays ?? 0}/${challenge.progressPercent ?? 0}%`
                      : challenge.type?.replace(/_/g, ' ') || `👥 ${challenge.participants}`}
                  </Text>
                </View>
              </View>
            </View>
            <TouchableOpacity style={styles.closeButtonTop} onPress={onClose}>
              <Text style={styles.closeButtonIcon}>✕</Text>
            </TouchableOpacity>
          </View>

          <View style={styles.detailSection}>
            <Text style={styles.detailSectionTitle}>Challenge Details</Text>
            <Text style={styles.aboutDescription}>{challenge.fullDescription}</Text>
            {getChallengeCriteriaText(challenge.type, challenge.targetValue) && (
              <View style={localStyles.infoBox}>
                <Text style={localStyles.infoBoxTitle}>Daily Goal:</Text>
                <Text style={localStyles.infoBoxText}>
                  {getChallengeCriteriaText(challenge.type, challenge.targetValue)}
                </Text>
              </View>
            )}
          </View>

          {hasActiveRun && (
            <View style={styles.detailSection}>
              <Text style={styles.detailSectionTitle}>Your Progress</Text>
              <View style={localStyles.progressCard}>
                <View style={localStyles.progressRow}>
                  <View style={localStyles.statBox}>
                    <Text style={localStyles.statNumber}>{challenge.completedDays ?? 0}</Text>
                    <Text style={localStyles.statLabel}>Days Done</Text>
                  </View>
                  <View style={localStyles.statBox}>
                    <Text style={localStyles.statNumber}>{challenge.streak ?? 0}</Text>
                    <Text style={localStyles.statLabel}>🔥 Streak</Text>
                  </View>
                  <View style={localStyles.statBox}>
                    <Text style={localStyles.statNumber}>{challenge.progressPercent ?? 0}%</Text>
                    <Text style={localStyles.statLabel}>Progress</Text>
                  </View>
                </View>
                <View style={styles.progressBar}>
                  <View
                    style={[styles.progressFill, { width: `${challenge.progressPercent ?? 0}%` }]}
                  />
                </View>
                <Text style={localStyles.progressText}>
                  {challenge.completedDays ?? 0} of {Math.ceil((100 / (challenge.progressPercent ?? 1)) * (challenge.completedDays ?? 1))} days completed
                </Text>
              </View>
              {challenge.startDate && challenge.endDate ? (
                <Text style={localStyles.dateText}>
                  📆 {challenge.startDate} to {challenge.endDate}
                </Text>
              ) : null}
            </View>
          )}

          <View style={styles.detailSection}>
            <Text style={styles.detailSectionTitle}>Reward Badge</Text>
            <View style={[styles.badgeCard, { backgroundColor: challenge.badge.badgeGradient }]}>
              <Text style={styles.badgeIcon}>{challenge.badge.icon}</Text>
              <Text style={styles.badgeName}>{challenge.badge.name}</Text>
              <Text style={styles.badgeDescription}>{challenge.badge.description}</Text>
            </View>
          </View>

          {challenge.checkins?.length ? (
            <View style={styles.detailSection}>
              <Text style={styles.detailSectionTitle}>
                Check-in History ({challenge.checkins.length})
              </Text>
              {challenge.checkins.map((checkin, index) => (
                <View key={`${checkin.date}-${index}`} style={localStyles.checkinItem}>
                  <View style={localStyles.checkinLeft}>
                    <Text style={localStyles.checkinStatus}>
                      {checkin.passed ? '✅' : '❌'}
                    </Text>
                    <View style={localStyles.checkinInfo}>
                      <Text style={localStyles.checkinDate}>{checkin.date}</Text>
                      <Text style={localStyles.checkinNote} numberOfLines={2}>
                        {checkin.note}
                      </Text>
                    </View>
                  </View>
                  <Text style={localStyles.checkinValue}>
                    {checkin.actualValue.toFixed(1)}
                  </Text>
                </View>
              ))}
            </View>
          ) : null}

          <View style={styles.detailSection}>
            <Text style={styles.detailSectionTitle}>Top Participants</Text>
            {challenge.topParticipants?.length ? (
              challenge.topParticipants.map((participant, index) => (
                <View key={index} style={styles.participantItem}>
                  <Image source={{ uri: participant.avatar }} style={styles.participantAvatar} />
                  <View style={styles.participantInfo}>
                    <Text style={styles.participantName}>{participant.name}</Text>
                    <View style={styles.progressBar}>
                      <View style={[styles.progressFill, { width: `${participant.progress}%` }]} />
                    </View>
                  </View>
                  <Text style={styles.progressText}>{participant.progress}%</Text>
                </View>
              ))
            ) : (
              <Text style={styles.aboutDescription}>
                Leaderboard data is not available yet.
              </Text>
            )}
          </View>

          <TouchableOpacity
            style={[styles.joinChallengeButton, hasActiveRun && styles.joinedButton]}
            onPress={() => onJoinPress(challenge.id)}
          >
            <Text style={styles.joinChallengeButtonText}>
              {hasActiveRun ? '✓ Active Challenge' : 'Join Challenge'}
            </Text>
          </TouchableOpacity>
          {hasActiveRun ? (
            <>
              <TouchableOpacity
                style={styles.joinChallengeButton}
                onPress={() => onCheckinPress(challenge.id)}
              >
                <Text style={styles.joinChallengeButtonText}>📝 Check in Today</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.joinChallengeButton, styles.joinedButton]}
                onPress={() => onAbandonPress(challenge.id)}
              >
                <Text style={styles.joinChallengeButtonText}>× Abandon Challenge</Text>
              </TouchableOpacity>
            </>
          ) : null}
        </ScrollView>
      </SafeAreaView>
    </Modal>
  );
}

const localStyles = StyleSheet.create({
  infoBox: {
    backgroundColor: 'rgba(0, 0, 0, 0.05)',
    borderRadius: 12,
    marginTop: 12,
    padding: 12,
  },
  infoBoxTitle: {
    fontSize: 12,
    fontWeight: '600',
    marginBottom: 6,
    opacity: 0.7,
  },
  infoBoxText: {
    fontSize: 14,
    lineHeight: 20,
  },
  progressCard: {
    backgroundColor: 'rgba(0, 0, 0, 0.05)',
    borderRadius: 12,
    marginBottom: 12,
    padding: 16,
  },
  progressRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 12,
  },
  statBox: {
    alignItems: 'center',
    flex: 1,
  },
  statNumber: {
    fontSize: 20,
    fontWeight: 'bold',
  },
  statLabel: {
    fontSize: 11,
    marginTop: 4,
    opacity: 0.7,
  },
  progressText: {
    fontSize: 12,
    marginTop: 12,
    opacity: 0.7,
    textAlign: 'center',
  },
  dateText: {
    fontSize: 12,
    marginTop: 8,
    opacity: 0.6,
  },
  checkinItem: {
    alignItems: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.05)',
    borderRadius: 10,
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
  },
  checkinLeft: {
    alignItems: 'center',
    flex: 1,
    flexDirection: 'row',
  },
  checkinStatus: {
    fontSize: 20,
    marginRight: 12,
  },
  checkinInfo: {
    flex: 1,
  },
  checkinDate: {
    fontSize: 13,
    fontWeight: '600',
  },
  checkinNote: {
    fontSize: 12,
    marginTop: 4,
    opacity: 0.7,
  },
  checkinValue: {
    fontSize: 12,
    fontWeight: '600',
    marginLeft: 12,
  },
});
